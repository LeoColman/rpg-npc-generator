package me.kerooker.portrait.queue

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json

/** Worker is disabled in these tests, so submitted jobs stay "queued" for deterministic assertions. */
private fun idleClient() = HttpClient(MockEngine) {
    engine { addHandler { respond("{}", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json")) } }
}

private val parseJson = Json { ignoreUnknownKeys = true }

private fun jobIdOf(body: String) = Regex("\"job_id\":\"([a-f0-9]+)\"").find(body)!!.groupValues[1]

class QueueRoutesTest : FunSpec({

    test("POST /submit returns a job id and queue position") {
        testApplication {
            application { queueModule(QueueService(idleClient(), "http://fastsd/api"), startWorker = false) }

            val response = client.post("/submit") {
                contentType(ContentType.Application.Json)
                setBody("""{"prompt":"a wizard"}""")
            }

            response.status shouldBe HttpStatusCode.OK
            val body = response.bodyAsText()
            body shouldContain "\"job_id\""
            body shouldContain "\"ahead\":0"
        }
    }

    test("GET /status returns the queued job with a queue length") {
        testApplication {
            application { queueModule(QueueService(idleClient(), "http://fastsd/api"), startWorker = false) }

            val submit = client.post("/submit") {
                contentType(ContentType.Application.Json)
                setBody("""{"prompt":"x"}""")
            }.bodyAsText()
            val status = client.get("/status/${jobIdOf(submit)}")

            status.status shouldBe HttpStatusCode.OK
            val body = status.bodyAsText()
            body shouldContain "\"state\":\"queued\""
            body shouldContain "\"queue_length\":1"
        }
    }

    test("GET /status of an unknown job is 404 with state unknown") {
        testApplication {
            application { queueModule(QueueService(idleClient(), "http://fastsd/api"), startWorker = false) }

            val response = client.get("/status/does-not-exist")

            response.status shouldBe HttpStatusCode.NotFound
            response.bodyAsText() shouldContain "\"state\":\"unknown\""
        }
    }

    test("DELETE /jobs cancels a queued job so it no longer occupies the line") {
        testApplication {
            application { queueModule(QueueService(idleClient(), "http://fastsd/api"), startWorker = false) }

            val submit = client.post("/submit") {
                contentType(ContentType.Application.Json)
                setBody("""{"prompt":"x"}""")
            }.bodyAsText()
            val jid = jobIdOf(submit)

            val cancel = client.delete("/jobs/$jid")

            cancel.status shouldBe HttpStatusCode.OK
            cancel.bodyAsText() shouldContain "\"cancelled\":true"
            client.get("/status/$jid").status shouldBe HttpStatusCode.NotFound
            client.get("/").bodyAsText() shouldContain "\"waiting\":0"
        }
    }

    test("DELETE /jobs of an unknown job is 404 with cancelled false") {
        testApplication {
            application { queueModule(QueueService(idleClient(), "http://fastsd/api"), startWorker = false) }

            val response = client.delete("/jobs/does-not-exist")

            response.status shouldBe HttpStatusCode.NotFound
            response.bodyAsText() shouldContain "\"cancelled\":false"
        }
    }

    test("POST /submit with invalid JSON is rejected with 400") {
        testApplication {
            application { queueModule(QueueService(idleClient(), "http://fastsd/api"), startWorker = false) }

            val response = client.post("/submit") {
                contentType(ContentType.Application.Json)
                setBody("not json")
            }

            response.status shouldBe HttpStatusCode.BadRequest
        }
    }

    test("GET / reports health and counters") {
        testApplication {
            application { queueModule(QueueService(idleClient(), "http://fastsd/api"), startWorker = false) }

            val response = client.get("/")

            response.status shouldBe HttpStatusCode.OK
            val body = response.bodyAsText()
            body shouldContain "\"ok\":true"
            body shouldContain "\"waiting\":0"
        }
    }

    test("GET /queue reports the queue size and counts queued jobs") {
        testApplication {
            application { queueModule(QueueService(idleClient(), "http://fastsd/api"), startWorker = false) }

            val empty = client.get("/queue")
            empty.status shouldBe HttpStatusCode.OK
            parseJson.decodeFromString(QueueSizeResponse.serializer(), empty.bodyAsText()) shouldBe
                QueueSizeResponse(size = 0, waiting = 0, processing = false)

            client.post("/submit") {
                contentType(ContentType.Application.Json)
                setBody("""{"prompt":"x"}""")
            }

            parseJson.decodeFromString(QueueSizeResponse.serializer(), client.get("/queue").bodyAsText()) shouldBe
                QueueSizeResponse(size = 1, waiting = 1, processing = false)
        }
    }
})
