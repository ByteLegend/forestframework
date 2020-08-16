package io.forestframework.example.realtimeauctions

import geb.Browser
import geb.Configuration
import io.forestframework.testfixtures.AbstractEndToEndTest
import io.forestframework.testfixtures.EndToEndTest
import io.forestframework.testsupport.ForestExtension
import io.forestframework.testsupport.ForestTest
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(ForestExtension.class)
@ForestTest(appClass = RealtimeAuctionsApp.class)
class RealtimeAuctionsEndToEndTest extends AbstractEndToEndTest {
    @EndToEndTest
    void 'realtime auctions test'(Configuration configuration) {
        Browser.drive(configuration) {
            baseUrl = "http://localhost:${port}/"
            go "/index.html"

            assert title == 'The best realtime auctions!'

            waitFor {
                $('#current_price').text().contains("EUR")
            }

            // EUR (\d+).00
            int initPrice = (($('#current_price').text() =~ /EUR (\d+)\.00/)[0][1]).toString().toInteger()

            $("#my_bid_value").value(initPrice + 1)

            $("#bid_button").click()

            waitFor {
                $("#feed").value().toString().contains("New offer: EUR ${initPrice + 1}.00")
            }

            $("#my_bid_value").value(initPrice + 2)

            $("#bid_button").click()

            waitFor {
                $("#feed").value().toString().contains("New offer: EUR ${initPrice + 2}.00")
            }

            $("#bid_button").click()

            waitFor {
                $("#error_message").text().contains("Invalid price")
            }
        }
    }
}


