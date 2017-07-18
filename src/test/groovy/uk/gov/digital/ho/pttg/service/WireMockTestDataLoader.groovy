package uk.gov.digital.ho.pttg.service

import com.github.tomakehurst.wiremock.WireMockServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static com.github.tomakehurst.wiremock.client.WireMock.*

class WireMockTestDataLoader {


    private static Logger LOGGER = LoggerFactory.getLogger(WireMockTestDataLoader.class);

    def dataDirName = "json"

    def WireMockServer wireMockServer

    WireMockTestDataLoader(int port) {
        wireMockServer = new WireMockServer(port)
        wireMockServer.start()
        configureFor("localhost", port);

        sleep(2000)

        LOGGER.debug("")
        LOGGER.debug("")
        LOGGER.debug("")
        LOGGER.debug("STARTED Wiremock server. Running = {}", wireMockServer.running)
        LOGGER.debug("")
        LOGGER.debug("")
        LOGGER.debug("")
    }

    def stubTestData(String fileName, String url) {

        def json = jsonFromFile(fileName)

        if (json == null) {
            assert false: "No test data file was loaded for $fileName from the resources/json directory - " +
                    "Please add it or check filename is correct"
        }

        addStub(json, url)
    }

    private def jsonFromFile(String fileName) {

        println ''
        def fileLocation = "/$dataDirName/$fileName" + ".json"
        LOGGER.debug("Loading test data for {}", fileLocation.toString())

        def file = this.getClass().getResource(fileLocation)

        if (file == null) {
            return null
        }

        return file.text
    }

    def addStub(String json, String url) {

        println ''
        LOGGER.debug("Stubbing Response data with $json")

        stubFor(get(urlPathMatching(url))
                .willReturn(aResponse()
                .withBody(json)
                .withHeader("Content-Type", "application/json")
                .withStatus(200)));

        println ''
        LOGGER.debug("Completed Stubbing Response data with $json")
    }


    def withServiceDown() {
        stop()
    }

    def stop() {
        wireMockServer.stop()
    }

    def verifyGetCount(int count, String url){
        verify(count, getRequestedFor(urlPathMatching(url)))
    }
}
