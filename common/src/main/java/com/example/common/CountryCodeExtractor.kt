package com.example.common

import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import com.rokobit.adstvv_unit.loger.SmartLog

object CountryCodeExtractor {
    private val isoCodeMap = mapOf(
        "93" to "af",
        "355" to "al",
        "213" to "dz",
        "1684" to "as",
        "376" to "ad",
        "244" to "ao",
        "1264" to "ai",
        "672" to "aq",
        "1268" to "ag",
        "54" to "ar",
        "374" to "am",
        "297" to "aw",
        "61" to "cc",
        "43" to "at",
        "994" to "az",
        "1242" to "bs",
        "973" to "bh",
        "880" to "bd",
        "1246" to "bb",
        "375" to "by",
        "32" to "be",
        "501" to "bz",
        "229" to "bj",
        "1441" to "bm",
        "975" to "bt",
        "591" to "bo",
        "387" to "ba",
        "267" to "bw",
        "55" to "br",
        "246" to "io",
        "1284" to "vg",
        "673" to "bn",
        "359" to "bg",
        "226" to "bf",
        "257" to "bi",
        "855" to "kh",
        "237" to "cm",
        "1" to "us",
        "238" to "cv",
        "1345" to "ky",
        "236" to "cf",
        "235" to "td",
        "56" to "cl",
        "86" to "cn",
        "57" to "co",
        "269" to "km",
        "682" to "ck",
        "506" to "cr",
        "385" to "hr",
        "53" to "cu",
        "599" to "an",
        "357" to "cy",
        "420" to "cz",
        "243" to "cd",
        "45" to "dk",
        "253" to "dj",
        "1767" to "dm",
        "1809" to "do",
        "1829" to "do",
        "1849" to "do",
        "670" to "tl",
        "593" to "ec",
        "20" to "eg",
        "503" to "sv",
        "240" to "gq",
        "291" to "er",
        "372" to "ee",
        "251" to "et",
        "500" to "fk",
        "298" to "fo",
        "679" to "fj",
        "358" to "fi",
        "33" to "fr",
        "689" to "pf",
        "241" to "ga",
        "220" to "gm",
        "995" to "ge",
        "49" to "de",
        "233" to "gh",
        "350" to "gi",
        "30" to "gr",
        "299" to "gl",
        "1473" to "gd",
        "1671" to "gu",
        "502" to "gt",
        "441481" to "gg",
        "224" to "gn",
        "245" to "gw",
        "592" to "gy",
        "509" to "ht",
        "504" to "hn",
        "852" to "hk",
        "36" to "hu",
        "354" to "is",
        "91" to "in",
        "62" to "id",
        "98" to "ir",
        "964" to "iq",
        "353" to "ie",
        "441624" to "im",
        "972" to "il",
        "39" to "it",
        "225" to "ci",
        "1876" to "jm",
        "81" to "jp",
        "441534" to "je",
        "962" to "jo",
        "7" to "ru",
        "254" to "ke",
        "686" to "ki",
        "383" to "xk",
        "965" to "kw",
        "996" to "kg",
        "856" to "la",
        "371" to "lv",
        "961" to "lb",
        "266" to "ls",
        "231" to "lr",
        "218" to "ly",
        "423" to "li",
        "370" to "lt",
        "352" to "lu",
        "853" to "mo",
        "389" to "mk",
        "261" to "mg",
        "265" to "mw",
        "60" to "my",
        "960" to "mv",
        "223" to "ml",
        "356" to "mt",
        "692" to "mh",
        "222" to "mr",
        "230" to "mu",
        "262" to "re",
        "52" to "mx",
        "691" to "fm",
        "373" to "md",
        "377" to "mc",
        "976" to "mn",
        "382" to "me",
        "1664" to "ms",
        "212" to "eh",
        "258" to "mz",
        "95" to "mm",
        "264" to "na",
        "674" to "nr",
        "977" to "np",
        "31" to "nl",
        "687" to "nc",
        "64" to "pn",
        "505" to "ni",
        "227" to "ne",
        "234" to "ng",
        "683" to "nu",
        "850" to "kp",
        "1670" to "mp",
        "47" to "sj",
        "968" to "om",
        "92" to "pk",
        "680" to "pw",
        "970" to "ps",
        "507" to "pa",
        "675" to "pg",
        "595" to "py",
        "51" to "pe",
        "63" to "ph",
        "48" to "pl",
        "351" to "pt",
        "1787" to "pr",
        "1939" to "pr",
        "974" to "qa",
        "242" to "cg",
        "40" to "ro",
        "250" to "rw",
        "590" to "mf",
        "290" to "sh",
        "1869" to "kn",
        "1758" to "lc",
        "508" to "pm",
        "1784" to "vc",
        "685" to "ws",
        "378" to "sm",
        "239" to "st",
        "966" to "sa",
        "221" to "sn",
        "381" to "rs",
        "248" to "sc",
        "232" to "sl",
        "65" to "sg",
        "1721" to "sx",
        "421" to "sk",
        "386" to "si",
        "677" to "sb",
        "252" to "so",
        "27" to "za",
        "82" to "kr",
        "211" to "ss",
        "34" to "es",
        "94" to "lk",
        "249" to "sd",
        "597" to "sr",
        "268" to "sz",
        "46" to "se",
        "41" to "ch",
        "963" to "sy",
        "886" to "tw",
        "992" to "tj",
        "255" to "tz",
        "66" to "th",
        "228" to "tg",
        "690" to "tk",
        "676" to "to",
        "1868" to "tt",
        "216" to "tn",
        "90" to "tr",
        "993" to "tm",
        "1649" to "tc",
        "688" to "tv",
        "1340" to "vi",
        "256" to "ug",
        "380" to "ua",
        "971" to "ae",
        "44" to "gb",
        "598" to "uy",
        "998" to "uz",
        "678" to "vu",
        "379" to "va",
        "58" to "ve",
        "84" to "vn",
        "681" to "wf",
        "967" to "ye",
        "260" to "zm",
        "263" to "zw"
    )

    private val countryCodeToPhoneCode = mapOf(
        "af" to "93",
        "al" to "355",
        "dz" to "213",
        "as" to "1",
        "ad" to "376",
        "ao" to "244",
        "ai" to "1",
        "aq" to "672",
        "ag" to "1",
        "ar" to "54",
        "am" to "374",
        "aw" to "297",
        "cc" to "61",
        "at" to "43",
        "az" to "994",
        "bs" to "1",
        "bh" to "973",
        "bd" to "880",
        "bb" to "1",
        "by" to "375",
        "be" to "32",
        "bz" to "501",
        "bj" to "229",
        "bm" to "1",
        "bt" to "975",
        "bo" to "591",
        "ba" to "387",
        "bw" to "267",
        "br" to "55",
        "io" to "246",
        "vg" to "1",
        "bn" to "673",
        "bg" to "359",
        "bf" to "226",
        "bi" to "257",
        "kh" to "855",
        "cm" to "237",
        "us" to "1",
        "cv" to "238",
        "ky" to "1",
        "cf" to "236",
        "td" to "235",
        "cl" to "56",
        "cn" to "86",
        "co" to "57",
        "km" to "269",
        "ck" to "682",
        "cr" to "506",
        "hr" to "385",
        "cu" to "53",
        "an" to "599",
        "cy" to "357",
        "cz" to "420",
        "cd" to "243",
        "dk" to "45",
        "dj" to "253",
        "dm" to "1",
        "do" to "1",
        "tl" to "670",
        "ec" to "593",
        "eg" to "20",
        "sv" to "503",
        "gq" to "240",
        "er" to "291",
        "ee" to "372",
        "et" to "251",
        "fk" to "500",
        "fo" to "298",
        "fj" to "679",
        "fi" to "358",
        "fr" to "33",
        "pf" to "689",
        "ga" to "241",
        "gm" to "220",
        "ge" to "995",
        "de" to "49",
        "gh" to "233",
        "gi" to "350",
        "gr" to "30",
        "gl" to "299",
        "gd" to "1",
        "gu" to "1",
        "gt" to "502",
        "gg" to "44",
        "gn" to "224",
        "gw" to "245",
        "gy" to "592",
        "ht" to "509",
        "hn" to "504",
        "hk" to "852",
        "hu" to "36",
        "is" to "354",
        "in" to "91",
        "id" to "62",
        "ir" to "98",
        "iq" to "964",
        "ie" to "353",
        "im" to "44",
        "il" to "972",
        "it" to "39",
        "ci" to "225",
        "jm" to "1",
        "jp" to "81",
        "je" to "44",
        "jo" to "962",
        "ru" to "7",
        "ke" to "254",
        "ki" to "686",
        "xk" to "383",
        "kw" to "965",
        "kg" to "996",
        "la" to "856",
        "lv" to "371",
        "lb" to "961",
        "ls" to "266",
        "lr" to "231",
        "ly" to "218",
        "li" to "423",
        "lt" to "370",
        "lu" to "352",
        "mo" to "853",
        "mk" to "389",
        "mg" to "261",
        "mw" to "265",
        "my" to "60",
        "mv" to "960",
        "ml" to "223",
        "mt" to "356",
        "mh" to "692",
        "mr" to "222",
        "mu" to "230",
        "re" to "262",
        "mx" to "52",
        "fm" to "691",
        "md" to "373",
        "mc" to "377",
        "mn" to "976",
        "me" to "382",
        "ms" to "1",
        "eh" to "212",
        "mz" to "258",
        "mm" to "95",
        "na" to "264",
        "nr" to "674",
        "np" to "977",
        "nl" to "31",
        "nc" to "687",
        "pn" to "64",
        "ni" to "505",
        "ne" to "227",
        "ng" to "234",
        "nu" to "683",
        "kp" to "850",
        "mp" to "1",
        "sj" to "47",
        "om" to "968",
        "pk" to "92",
        "pw" to "680",
        "ps" to "970",
        "pa" to "507",
        "pg" to "675",
        "py" to "595",
        "pe" to "51",
        "ph" to "63",
        "pl" to "48",
        "pt" to "351",
        "pr" to "1",
        "qa" to "974",
        "cg" to "242",
        "ro" to "40",
        "rw" to "250",
        "mf" to "590",
        "sh" to "290",
        "kn" to "1",
        "lc" to "1",
        "pm" to "508",
        "vc" to "1",
        "ws" to "685",
        "sm" to "378",
        "st" to "239",
        "sa" to "966",
        "sn" to "221",
        "rs" to "381",
        "sc" to "248",
        "sl" to "232",
        "sg" to "65",
        "sx" to "1",
        "sk" to "421",
        "si" to "386",
        "sb" to "677",
        "so" to "252",
        "za" to "27",
        "kr" to "82",
        "ss" to "211",
        "es" to "34",
        "lk" to "94",
        "sd" to "249",
        "sr" to "597",
        "sz" to "268",
        "se" to "46",
        "ch" to "41",
        "sy" to "963",
        "tw" to "886",
        "tj" to "992",
        "tz" to "255",
        "th" to "66",
        "tg" to "228",
        "tk" to "690",
        "to" to "676",
        "tt" to "1",
        "tn" to "216",
        "tr" to "90",
        "tm" to "993",
        "tc" to "1",
        "tv" to "688",
        "vi" to "1",
        "ug" to "256",
        "ua" to "380",
        "ae" to "971",
        "gb" to "44",
        "uy" to "598",
        "uz" to "998",
        "vu" to "678",
        "va" to "379",
        "ve" to "58",
        "vn" to "84",
        "wf" to "681",
        "ye" to "967",
        "zm" to "260",
        "zw" to "263"
    )

    fun getCountryCode(context: Context): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val simInfo = SimUtil.getSIMInfo(context)
        SmartLog.e("Network operator ${tm.networkOperator}")
        return simInfo?.firstOrNull()?.countryIso?.ifEmpty {
            tm.networkCountryIso?.ifEmpty {
                getCountryCodeFromIccId(simInfo.firstOrNull()?.iccId)
            }
        } ?: getCountryCodeFromIccId(simInfo?.firstOrNull()?.iccId)
    }

    private fun getCountryCodeFromIccId(iccId: String?): String {
        if (iccId == null)
            return "default"
        val iccIdWithoutDefaultDigit = iccId.substring(2)
        isoCodeMap.forEach {
            if (iccIdWithoutDefaultDigit.startsWith(it.key)) {
                return it.value
            }
        }
        return "default"
    }

    fun getCountryPhoneCode(simId: String): String? {
        val countryCode = getCountryCodeFromIccId(simId)
        Log.e("CountryPhoneCode", countryCodeToPhoneCode[countryCode] ?: "")
        return countryCodeToPhoneCode[countryCode]
    }
}