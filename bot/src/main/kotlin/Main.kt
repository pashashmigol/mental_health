import Settings.CREDENTIALS_FILE_NAME
import com.google.auth.oauth2.GoogleCredentials
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.io.FileInputStream

private const val TOKEN = "1466693925:AAGkgMILgAg4gvs4Ct6UFlPM5T9VJx0BoIY"

private val serviceAccount = FileInputStream(CREDENTIALS_FILE_NAME)
val credentials: GoogleCredentials = GoogleCredentials.fromStream(serviceAccount)

fun Application.main() {
    val bot = launchBot(testingMode = false, token = TOKEN)

    routing {
        get("/status") {
            call.respond("Server is running!")
        }
        post("/$TOKEN") {
            val response = call.receiveText()
            bot.processUpdate(response)
            call.respond(HttpStatusCode.OK)
        }
    }
}