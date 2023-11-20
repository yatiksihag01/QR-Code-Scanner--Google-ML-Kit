package com.yatik.qrscanner.utils

/*
 * Copyright 2023 Yatik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
                "    \"brands\": \"Sample brand\",\n" +
                "    \"image_front_small_url\": \"https://\",\n" +
                "    \"nutriments\": {\n" +
                "      \"carbohydrates\": 54,\n" +
                "      \"carbohydrates_100g\": 54,\n" +
                "      \"carbohydrates_serving\": 8.1,\n" +
                "      \"energy\": 2270,\n" +
                "      \"energy-kcal\": 544,\n" +
                "      \"energy-kcal_100g\": 544,\n" +
                "      \"energy-kcal_serving\": 81.6,\n" +
                "      \"fat_100g\": 32,\n" +
                "      \"fat_serving\": 4.8,\n" +
                "      \"fiber\": 3.6,\n" +
                "      \"fiber_100g\": 3.6,\n" +
                "      \"fiber_serving\": 0.54,\n" +
                "      \"nova-group\": 4,\n" +
                "      \"nova-group_100g\": 4,\n" +
                "      \"nova-group_serving\": 4,\n" +
                "      \"proteins\": 8.1,\n" +
                "      \"proteins_100g\": 8.1,\n" +
                "      \"proteins_serving\": 1.22,\n" +
                "      \"salt\": 0.12,\n" +
                "      \"salt_100g\": 0.12,\n" +
                "      \"salt_serving\": 0.018,\n" +
                "      \"salt_unit\": \"g\",\n" +
                "      \"salt_value\": 0.12,\n" +
                "      \"sodium\": 0.048,\n" +
                "      \"sodium_100g\": 0.048,\n" +
                "      \"sodium_serving\": 0.0072,\n" +
                "      \"sodium_unit\": \"g\",\n" +
                "      \"sodium_value\": 0.048,\n" +
                "      \"sugars\": 51,\n" +
                "      \"sugars_100g\": 51,\n" +
                "      \"sugars_serving\": 7.65,\n" +
                "      \"sugars_unit\": \"g\",\n" +
                "      \"sugars_value\": 51\n" +
                "    },\n" +
                "    \"nutriscore_data\": {\n" +
                "      \"energy\": 2270,\n" +
                "      \"grade\": \"d\",\n" +
                "      \"is_beverage\": 0,\n" +
                "      \"is_cheese\": 0,\n" +
                "      \"is_fat\": 0,\n" +
                "      \"is_water\": 0\n" +
                "    },\n" +
                "    \"nutrition_grades\": \"d\",\n" +
                "    \"product_name\": \"Biscuit\",\n" +
                "    \"quantity\": \"270 g\"\n" +
                "  },\n" +
                "  \"status\": 1,\n" +
                "  \"status_verbose\": \"product found\"\n" +
                "}"

        const val ERROR_FOOD_RESPONSE =
            "{\"code\":\"123\",\"status\":0,\"status_verbose\":\"product not found\"}"

        const val ITEMS_PER_PAGE = 10

    }

}