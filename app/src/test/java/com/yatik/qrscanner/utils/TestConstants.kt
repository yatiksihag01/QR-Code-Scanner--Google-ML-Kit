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

        const val SAMPLE_FOOD_BARCODE = "8001505005592"

        const val SUCCESSFUL_FOOD_RESPONSE = "{\n" +
                "  \"code\": \"8001505005592\",\n" +
                "  \"product\": {\n" +
                "    \"nutriments\": {\n" +
                "      \"carbohydrates\": 54,\n" +
                "      \"carbohydrates_100g\": 54,\n" +
                "      \"carbohydrates_serving\": 8.1,\n" +
                "      \"carbohydrates_unit\": \"g\",\n" +
                "      \"carbohydrates_value\": 54,\n" +
                "      \"energy_100g\": 2270,\n" +
                "      \"energy_serving\": 340,\n" +
                "      \"energy_unit\": \"kJ\",\n" +
                "      \"energy_value\": 2270,\n" +
                "      \"fat\": 32,\n" +
                "      \"nova-group\": 4,\n" +
                "      \"proteins\": 8.1,\n" +
                "      \"proteins_100g\": 8.1,\n" +
                "      \"proteins_serving\": 1.22,\n" +
                "      \"proteins_unit\": \"g\",\n" +
                "      \"proteins_value\": 8.1,\n" +
                "      \"salt\": 0.12,\n" +
                "      \"saturated-fat\": 5.7,\n" +
                "      \"saturated-fat_100g\": 5.7,\n" +
                "      \"saturated-fat_serving\": 0.855,\n" +
                "      \"saturated-fat_unit\": \"g\",\n" +
                "      \"saturated-fat_value\": 5.7,\n" +
                "      \"sodium\": 0.048,\n" +
                "      \"sugars\": 51,\n" +
                "      \"sugars_100g\": 51,\n" +
                "      \"sugars_serving\": 7.65,\n" +
                "      \"sugars_unit\": \"g\",\n" +
                "      \"sugars_value\": 51\n" +
                "    },\n" +
                "    \"nutriscore_data\": {\n" +
                "      \"energy\": 2270,\n" +
                "      \"grade\": \"d\",\n" +
                "      \"negative_points\": 21,\n" +
                "      \"positive_points\": 3,\n" +
                "      \"proteins\": 8.1\n" +
                "    },\n" +
                "    \"nutrition_grades\": \"d\",\n" +
                "    \"product_name\": \"Biscuit\"\n" +
                "  },\n" +
                "  \"status\": 1,\n" +
                "  \"status_verbose\": \"product found\"}"

        const val ERROR_FOOD_RESPONSE =
            "{\"code\":\"123\",\"status\":0,\"status_verbose\":\"product not found\"}"

    }

}