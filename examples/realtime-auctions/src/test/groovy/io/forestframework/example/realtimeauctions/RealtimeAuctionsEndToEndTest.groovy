package io.forestframework.example.realtimeauctions

import geb.Browser
import geb.Configuration
import io.forestframework.testfixtures.AbstractEndToEndTest
import io.forestframework.testfixtures.EndToEndTest
import io.forestframework.testsupport.ForestExtension
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ForestExtension.class)
@Disabled
//@ForestTest(appClass = WebSocketChatRoomEndToEndTestApp.class)
class RealtimeAuctionsEndToEndTest extends AbstractEndToEndTest {
    @EndToEndTest
    void 'realtime auctions test'(Configuration configuration) {
        Browser.drive(configuration) {
            baseUrl = "http://localhost:${port}/"
            go "/index.html"

            assert title == 'The best realtime auctions!'

            waitFor {
                $('#current_price').text() == "EUR 0.00"
            }

            $("#my_bid_value").value "1"

            $("#bid_button").click()

            waitFor {
                $("#feed").value().toString().contains("New offer: EUR 1.00")
            }

            $("#my_bid_value").value "2"

            $("#bid_button").click()

            waitFor {
                $("#feed").value().toString().contains("New offer: EUR 2.00")
            }

            $("#bid_button").click()

            waitFor {
                $("#error_message").text().contains("Invalid price")
            }
        }
    }
}


