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

class Constants {

    companion object {

        const val POLICIES_LINK = "https://sites.google.com/view/yatik-qr-scanner/home"
        const val SUPPORT_MAIL = "mailto:yatikapps@outlook.com"
        const val PLAY_STORE = "https://play.google.com/store/apps/details?id=com.yatik.qrscanner"
        const val GALAXY_STORE =
            "https://apps.samsung.com/appquery/appDetail.as?appId=com.yatik.qrscanner"
        const val SHARE_APP_MESSAGE =
            "Hey!! check out this awesome QR Scanner app at Play Store: ${PLAY_STORE}\n" +
                    " and at Galaxy Store: $GALAXY_STORE"
        const val SHEET_PEEK_VAL = 100
        const val QR_WIDTH_HEIGHT = 600

        const val ITEMS_PER_PAGE = 50

        /** 24 hours in millis */
        const val PRODUCT_CACHE_TIME: Long = 24 * 60 * 60 * 1000

    }

}