package com.yatik.qrscanner.utils

class TestConstants {

    companion object {

        const val SUCCESS_RESPONSE_CODE = 200
        const val ERROR_RESPONSE_CODE = 404

        const val MAIN_URL = "https://testurl.com"
        const val SUCCESS_RESPONSE_TITLE = "Example Page"
        const val SUCCESS_RESPONSE_DESCRIPTION = "This is an example page."
        const val SUCCESS_RESPONSE_IMAGE_URL = "https://testurl.com/image.jpg"

        const val SUCCESS_RESPONSE = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <title>$SUCCESS_RESPONSE_TITLE</title>\n" +
                "    <meta name=\"description\" content=\"$SUCCESS_RESPONSE_DESCRIPTION\">\n" +
                "    <meta property=\"og:image\" content=\"$SUCCESS_RESPONSE_IMAGE_URL\">\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <h1>Welcome to the Example Page!</h1>\n" +
                "    <p>This is an example page that demonstrates how to create a preview for a webpage.</p>\n" +
                "    <img src=\"$SUCCESS_RESPONSE_IMAGE_URL\" alt=\"Example Image\">\n" +
                "  </body>\n" +
                "</html>\n"

        const val RESPONSE_ON_ERROR_CODE = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <title>Page Not Found</title>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <h1>404 Error: Page Not Found</h1>\n" +
                "    <p>We're sorry, but the page you're looking for cannot be found.</p>\n" +
                "  </body>\n" +
                "</html>\n"

    }

}