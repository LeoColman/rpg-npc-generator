package me.kerooker.portrait.queue

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import java.io.IOException

/** FastSD stand-in that always returns [body] with [status]. */
private fun clientReturning(body: String, status: HttpStatusCode = HttpStatusCode.OK) =
    HttpClient(MockEngine) {
        engine {
            addHandler { respond(body, status, headersOf(HttpHeaders.ContentType, "application/json")) }
        }
    }

/** FastSD stand-in that fails the call (connection error, timeout, ...). */
private fun clientThrowing(message: String) =
    HttpClient(MockEngine) {
        engine { addHandler { throw IOException(message) } }
    }

private const val DONE_BODY = """{"images":["QUJD"]}"""

class QueueServiceTest : FunSpec({

    test("submit assigns ahead by queue position") {
        val service = QueueService(clientReturning(DONE_BODY), "http://fastsd/api")

        service.submit("""{"n":1}""").ahead shouldBe 0
        service.submit("""{"n":2}""").ahead shouldBe 1
        service.submit("""{"n":3}""").ahead shouldBe 2
    }

    test("a freshly submitted job is queued with a full queue_length") {
        val service = QueueService(clientReturning(DONE_BODY), "http://fastsd/api")

        val id = service.submit("""{"prompt":"x"}""").jobId
        val status = service.status(id)!!

        status.state shouldBe "queued"
        status.ahead shouldBe 0
        status.queueLength shouldBe 1
        status.image.shouldBeNull()
        status.error.shouldBeNull()
    }

    test("processNext renders a job to done and returns the FastSD image") {
        val service = QueueService(clientReturning(DONE_BODY), "http://fastsd/api")
        val id = service.submit("{}").jobId

        service.processNext() shouldBe true

        val status = service.status(id)!!
        status.state shouldBe "done"
        status.image shouldBe "QUJD"
        status.ahead shouldBe 0
        status.queueLength shouldBe 0
    }

    test("processNext returns false when the queue is empty") {
        val service = QueueService(clientReturning(DONE_BODY), "http://fastsd/api")
        service.processNext() shouldBe false
    }

    test("jobs are drained in FIFO order") {
        val service = QueueService(clientReturning(DONE_BODY), "http://fastsd/api")
        val first = service.submit("""{"n":"a"}""").jobId
        val second = service.submit("""{"n":"b"}""").jobId

        service.processNext() shouldBe true
        service.status(first)!!.state shouldBe "done"
        service.status(second)!!.state shouldBe "queued"

        service.processNext() shouldBe true
        service.status(second)!!.state shouldBe "done"
        service.processNext() shouldBe false
    }

    test("a FastSD error field surfaces as an error job") {
        val service = QueueService(clientReturning("""{"error":"boom"}"""), "http://fastsd/api")
        val id = service.submit("{}").jobId

        service.processNext()

        val status = service.status(id)!!
        status.state shouldBe "error"
        status.error shouldBe "boom"
    }

    test("a response with no image and no error reports the HTTP status") {
        val service = QueueService(clientReturning("{}"), "http://fastsd/api")
        val id = service.submit("{}").jobId

        service.processNext()

        val status = service.status(id)!!
        status.state shouldBe "error"
        status.error shouldBe "no image (HTTP 200)"
    }

    test("a failed FastSD call becomes an error job carrying the reason") {
        val service = QueueService(clientThrowing("connection refused"), "http://fastsd/api")
        val id = service.submit("{}").jobId

        service.processNext()

        val status = service.status(id)!!
        status.state shouldBe "error"
        status.error!! shouldContain "connection refused"
    }

    test("status of an unknown job is null") {
        val service = QueueService(clientReturning(DONE_BODY), "http://fastsd/api")
        service.status("does-not-exist").shouldBeNull()
    }

    test("cancel removes a queued job and frees the line for the rest") {
        val service = QueueService(clientReturning(DONE_BODY), "http://fastsd/api")
        val first = service.submit("""{"n":1}""").jobId
        val second = service.submit("""{"n":2}""").jobId

        service.cancel(first) shouldBe true

        service.status(first).shouldBeNull()
        service.status(second)!!.ahead shouldBe 0 // was 1; now first in line
        service.snapshot().waiting shouldBe 1
    }

    test("cancel of an unknown job returns false") {
        val service = QueueService(clientReturning(DONE_BODY), "http://fastsd/api")
        service.cancel("does-not-exist") shouldBe false
    }

    test("cancel forgets a finished job") {
        val service = QueueService(clientReturning(DONE_BODY), "http://fastsd/api")
        val id = service.submit("{}").jobId
        service.processNext()
        service.status(id)!!.state shouldBe "done"

        service.cancel(id) shouldBe true
        service.status(id).shouldBeNull()
    }

    test("finished jobs are pruned after the TTL elapses") {
        var clock = 1_000L
        val service = QueueService(
            client = clientReturning(DONE_BODY),
            fastsdUrl = "http://fastsd/api",
            jobTtlSeconds = 10,
            clock = { clock },
        )
        val id = service.submit("{}").jobId
        service.processNext()
        service.status(id)!!.state shouldBe "done"

        clock = 1_011L // past updated + TTL
        service.status(id).shouldBeNull()
    }

    test("finished jobs survive until the TTL elapses") {
        var clock = 1_000L
        val service = QueueService(
            client = clientReturning(DONE_BODY),
            fastsdUrl = "http://fastsd/api",
            jobTtlSeconds = 10,
            clock = { clock },
        )
        val id = service.submit("{}").jobId
        service.processNext()

        clock = 1_009L // still within TTL
        service.status(id)!!.state shouldBe "done"
    }
})
