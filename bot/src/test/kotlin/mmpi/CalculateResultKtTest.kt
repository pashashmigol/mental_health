package mmpi

import Gender
import models.Type
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import storage.CentralDataStorage
import telegram.LaunchMode
import kotlin.math.roundToInt

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CalculateResultKtTest {
    private lateinit var scalesM: MmpiProcess.Scales
    private lateinit var scalesF: MmpiProcess.Scales

    @BeforeAll
    fun setup() {
        CentralDataStorage.init(
            rootPath = LaunchMode.TESTS.rootPath,
            testingMode = true
        )

        scalesM = CentralDataStorage.mmpi566Data.scales(Gender.Male)
        scalesF = CentralDataStorage.mmpi566Data.scales(Gender.Female)
    }

    @Test
    fun agree_with_everything() {
        val test = MmpiProcess(Gender.Male, Type.Mmpi566)

        allAgree566.forEachIndexed {index, answer ->
            test.submitAnswer(index, answer)
        }
        val result = test.calculateResult()
        assertNotNull(result)
    }

    @Test
    fun notCompletedTest() {
        val test = MmpiProcess(Gender.Male, Type.Mmpi566)

        Assertions.assertThrows(RuntimeException::class.java) {
            justFewAnswers.forEachIndexed { index, answer ->
                test.submitAnswer(index, answer)
            }
            test.calculateResult()
        }
    }

    @Test
    fun oneAfterOneTest() {
        val test = MmpiProcess(Gender.Male, Type.Mmpi377)

        oneAfterOne377.forEachIndexed { index, answer ->
            test.submitAnswer(index, answer)
        }
        val result = test.calculateResult()

        assertEquals(3, result.liesScaleL.raw)
        assertEquals(30, result.credibilityScaleF.raw)
        assertEquals(14, result.correctionScaleK.raw)
        assertEquals(20, result.overControlScale1.raw)
        assertEquals(25, result.passivityScale2.raw)
        assertEquals(23, result.labilityScale3.raw)
        assertEquals(31, result.impulsivenessScale4.raw)
        assertEquals(29, result.masculinityScale5.raw)
        assertEquals(14, result.rigidityScale6.raw)
        assertEquals(31, result.anxietyScale7.raw)
        assertEquals(48, result.individualismScale8.raw)
        assertEquals(23, result.optimismScale9.raw)
        assertEquals(31, result.introversionScale0.raw)


        assertEquals(46.0, result.liesScaleL.score.toDouble(), 2.0)
        assertEquals(132.0, result.credibilityScaleF.score.toDouble(), 2.0)
        assertEquals(45.0, result.correctionScaleK.score.toDouble(), 2.0)
        assertEquals(56.0, result.introversionScale0.score.toDouble(), 2.0)
        assertEquals(75.0, result.overControlScale1.score.toDouble(), 2.0)
        assertEquals(62.0, result.passivityScale2.score.toDouble(), 2.0)
        assertEquals(61.0, result.labilityScale3.score.toDouble(), 2.0)
        assertEquals(73.0, result.impulsivenessScale4.score.toDouble(), 2.0)
        assertEquals(72.0, result.masculinityScale5.score.toDouble(), 2.0)
        assertEquals(69.0, result.rigidityScale6.score.toDouble(), 2.0)
        assertEquals(55.0, result.anxietyScale7.score.toDouble(), 2.0)
        assertEquals(97.0, result.individualismScale8.score.toDouble(), 2.0)
        assertEquals(61.0, result.optimismScale9.score.toDouble(), 2.0)

        assertEquals("достоверные результаты", result.liesScaleL.description)

        assertEquals(true, result.liesScaleL.useRawValuesForDescription)
        assertEquals(false, result.credibilityScaleF.useRawValuesForDescription)
        assertEquals(false, result.correctionScaleK.useRawValuesForDescription)
        assertEquals(false, result.introversionScale0.useRawValuesForDescription)
        assertEquals(false, result.overControlScale1.useRawValuesForDescription)
        assertEquals(false, result.passivityScale2.useRawValuesForDescription)
        assertEquals(false, result.labilityScale3.useRawValuesForDescription)
        assertEquals(false, result.impulsivenessScale4.useRawValuesForDescription)
        assertEquals(false, result.masculinityScale5.useRawValuesForDescription)
        assertEquals(false, result.rigidityScale6.useRawValuesForDescription)
        assertEquals(false, result.anxietyScale7.useRawValuesForDescription)
        assertEquals(false, result.individualismScale8.useRawValuesForDescription)
        assertEquals(false, result.optimismScale9.useRawValuesForDescription)
    }

    @Test
    fun checkScales() {
        val scales: MmpiProcess.Scales = CentralDataStorage.mmpi566Data.scales(Gender.Female)
        assertEquals(15, scaleSum(scales.liesScaleL))
        assertEquals(64, scaleSum(scales.credibilityScaleF))
        assertEquals(30, scaleSum(scales.correctionScaleK))
        assertEquals(33, scaleSum(scales.overControlScale1))
        assertEquals(60, scaleSum(scales.passivityScale2))
        assertEquals(60, scaleSum(scales.labilityScale3))
        assertEquals(50, scaleSum(scales.impulsivenessScale4))
        assertEquals(60, scaleSum(scales.masculinityScale5))
        assertEquals(40, scaleSum(scales.rigidityScale6))
        assertEquals(48, scaleSum(scales.anxietyScale7))
        assertEquals(78, scaleSum(scales.individualismScale8))
        assertEquals(46, scaleSum(scales.optimismScale9))
        assertEquals(70, scaleSum(scales.introversionScale0))
    }

    @Test
    fun check566_K() {
        val agree = setOf(96)
        val disagree = setOf(
            30, 39, 71, 89, 124, 129, 134, 138, 142, 148, 160, 170, 171, 180,
            183, 217, 234, 267, 272, 296, 316, 322, 374, 383, 397, 398, 406, 461, 502
        )
        val resM = calculateScale(agree, disagree, Gender.Male)
        assertEquals(30, resM.correctionScaleK.raw)
        assertEquals(84, resM.correctionScaleK.score)

        val resF = calculateScale(agree, disagree, Gender.Female)
        assertEquals(30, resF.correctionScaleK.raw)
        assertEquals(84, resF.correctionScaleK.score)

        assertEquals(36, scalesM.correctionScaleK.finalScore(5))
        assertEquals(74, scalesM.correctionScaleK.finalScore(25))

        assertEquals(36, scalesF.correctionScaleK.finalScore(5))
        assertEquals(74, scalesF.correctionScaleK.finalScore(25))
    }

    @Test
    fun check566_1() {
        val agree = setOf(23, 29, 43, 62, 72, 108, 114, 125, 161, 189, 273)
        val disagree = setOf(
            2, 3, 7, 9, 18, 51, 55, 63, 68, 103, 130, 153,
            155, 163, 175, 188, 190, 192, 230, 243, 274, 281
        )
        val resM = calculateScale(agree, disagree, Gender.Male)

        assertEquals(
            33 + (resM.correctionScaleK.raw * 0.5).roundToInt(),
            resM.overControlScale1.raw
        )

        val resF = calculateScale(agree, disagree, Gender.Female)
        assertEquals(
            33 + (resF.correctionScaleK.raw * 0.5).roundToInt(),
            resF.overControlScale1.raw
        )
        assertEquals(121, resF.overControlScale1.score)

        assertEquals(34, scalesM.overControlScale1.finalScore(5))
        assertEquals(86, scalesM.overControlScale1.finalScore(25))

        assertEquals(34, scalesF.overControlScale1.finalScore(5))
        assertEquals(105, scalesF.overControlScale1.finalScore(40))
    }

    @Test
    fun check566_2() {
        val agree = setOf(
            5, 13, 23, 32, 41, 43, 52, 67, 86, 104, 130, 138, 142,
            158, 159, 182, 189, 193, 236, 259
        )
        val disagree = setOf(
            2, 8, 9, 18, 30, 36, 39, 46, 51, 57, 58, 64, 80, 88,
            89, 95, 98, 107, 122, 131, 145, 152, 153, 154, 155, 160, 178,
            191, 207, 208, 233, 241, 242, 248, 263, 270, 271, 272, 285, 296
        )
        val resM = calculateScale(agree, disagree, Gender.Male)
        assertEquals(60, resM.passivityScale2.raw)
        assertEquals(155, resM.passivityScale2.score)

        val resF = calculateScale(agree, disagree, Gender.Female)
        assertEquals(60, resF.passivityScale2.raw)
        assertEquals(128, resF.passivityScale2.score)

        assertEquals(34, scalesM.passivityScale2.finalScore(10))
        assertEquals(107, scalesM.passivityScale2.finalScore(40))

        assertEquals(33, scalesF.passivityScale2.finalScore(10))
        assertEquals(90, scalesF.passivityScale2.finalScore(40))
    }

    @Test
    fun check566_3() {
        val agree = setOf(
            10, 23, 32, 43, 44, 47, 76, 114, 179, 186, 189, 238, 253
        )
        val disagree = setOf(
            2, 3, 6, 7, 8, 9, 12, 26, 30, 51, 55, 71, 89, 93, 103,
            107, 109, 124, 128, 129, 136, 137, 141, 147, 153, 160, 162, 163,
            170, 172, 174, 175, 180, 188, 190, 192, 201, 213, 230, 234, 243,
            265, 267, 274, 279, 289, 292
        )
        val resM = calculateScale(agree, disagree, Gender.Male)
        assertEquals(60, resM.labilityScale3.raw)
        assertEquals(129, resM.labilityScale3.score)

        val resF = calculateScale(agree, disagree, Gender.Female)
        assertEquals(60, resF.labilityScale3.raw)
        assertEquals(122, resF.labilityScale3.score)

        assertEquals(39, scalesM.labilityScale3.finalScore(10))
        assertEquals(102, scalesM.labilityScale3.finalScore(45))

        assertEquals(33, scalesF.labilityScale3.finalScore(10))
        assertEquals(96, scalesF.labilityScale3.finalScore(45))
    }

    @Test
    fun check566_4() {
        val agree = setOf(
            16, 21, 24, 32, 33, 35, 38, 42, 61, 67, 84, 94, 102, 106,
            110, 118, 127, 215, 216, 224, 239, 244, 245, 284
        )
        val disagree = setOf(
            8, 20, 37, 82, 91, 96, 107, 134, 137, 141, 155, 170,
            171, 173, 180, 183, 201, 231, 235, 237, 248, 267, 287, 289, 294,
            296
        )
        val resM = calculateScale(agree, disagree, Gender.Male)
        assertEquals(50 + (resM.correctionScaleK.raw * 0.4).roundToInt(), resM.impulsivenessScale4.raw)
        assertEquals(151, resM.impulsivenessScale4.score)

        val resF = calculateScale(agree, disagree, Gender.Female)
        assertEquals(50 + (resF.correctionScaleK.raw * 0.4).roundToInt(), resF.impulsivenessScale4.raw)
        assertEquals(151, resF.impulsivenessScale4.score)


        assertEquals(40, scalesM.impulsivenessScale4.finalScore(15))
        assertEquals(99, scalesM.impulsivenessScale4.finalScore(40))

        assertEquals(40, scalesF.impulsivenessScale4.finalScore(15))
        assertEquals(99, scalesF.impulsivenessScale4.finalScore(40))
    }

    @Test
    fun check566_5() {
        val resM = calculateScale(
            agree = setOf(
                4, 25, 69, 70, 74, 77, 78, 87, 92, 126, 132, 134, 140, 149, 179,
                187, 203, 204, 217, 226, 231, 239, 261, 278, 282, 295, 297, 299
            ),
            disagree = setOf(
                1, 19, 26, 28, 79, 80, 81, 89, 99, 112, 115, 116, 117, 120, 133,
                144, 176, 198, 213, 214, 219, 221, 223, 229, 249, 254, 260, 262,
                264, 280, 283, 300
            ),
            gender = Gender.Male
        )
        assertEquals(60, resM.masculinityScale5.raw)
        assertEquals(127, resM.masculinityScale5.score)

        val resF = calculateScale(
            agree = setOf(
                4, 25, 70, 74, 77, 78, 87, 92, 126, 132, 133, 134, 140,
                149, 187, 203, 204, 217, 226, 239, 261, 278, 282, 295, 299
            ),
            disagree = setOf(
                1, 19, 26, 28, 69, 79, 80, 81, 89, 99, 112, 115, 116,
                117, 120, 144, 176, 179, 198, 213, 214, 219, 221, 223, 229, 231,
                249, 254, 260, 262, 264, 280, 283, 297, 300
            ),
            gender = Gender.Female
        )
        assertEquals(60, resF.masculinityScale5.raw)
        assertEquals(2, resF.masculinityScale5.score)

        assertEquals(29, scalesM.masculinityScale5.finalScore(10))
        assertEquals(97, scalesM.masculinityScale5.finalScore(45))

        assertEquals(33, scalesF.masculinityScale5.finalScore(45))
        assertEquals(95, scalesF.masculinityScale5.finalScore(15))
    }

    @Test
    fun check566_6() {
        val agree = setOf(
            15, 16, 22, 24, 27, 35, 110, 121, 123, 127, 151, 157,
            158, 202, 275, 284, 291, 293, 299, 305, 317, 338, 341, 364, 365
        )
        val disagree = setOf(
            93, 107, 109, 111, 117, 124, 268, 281, 294, 313, 316,
            319, 327, 347, 348
        )
        val resM = calculateScale(agree, disagree, Gender.Male)
        assertEquals(40, resM.rigidityScale6.raw)
        assertEquals(143, resM.rigidityScale6.score)

        val resF = calculateScale(agree, disagree, Gender.Female)
        assertEquals(40, resF.rigidityScale6.raw)
        assertEquals(143, resF.rigidityScale6.score)

        assertEquals(41, scalesM.rigidityScale6.finalScore(5))
        assertEquals(99, scalesM.rigidityScale6.finalScore(25))

        assertEquals(27, scalesF.rigidityScale6.finalScore(0))
        assertEquals(70, scalesF.rigidityScale6.finalScore(15))
    }

    @Test
    fun check566_7() {
        val agree = setOf(
            10, 15, 22, 32, 41, 67, 76, 86, 94, 102, 106, 142, 159,
            182, 189, 217, 238, 266, 301, 304, 305, 317, 321, 336, 337, 340,
            342, 343, 344, 346, 349, 351, 352, 356, 357, 358, 359, 360, 361
        )
        val disagree = setOf(
            3, 8, 36, 122, 152, 164, 178, 329, 353
        )
        val resM = calculateScale(agree, disagree, Gender.Male)
        assertEquals(48 + resM.correctionScaleK.raw, resM.anxietyScale7.raw)
        assertEquals(157, resM.anxietyScale7.score)

        val resF = calculateScale(agree, disagree, Gender.Female)
        assertEquals(48 + resF.correctionScaleK.raw, resF.anxietyScale7.raw)
        assertEquals(132, resF.anxietyScale7.score)

        assertEquals(22, scalesM.anxietyScale7.finalScore(10))
        assertEquals(105, scalesM.anxietyScale7.finalScore(50))

        assertEquals(41, scalesF.anxietyScale7.finalScore(20))
        assertEquals(91, scalesF.anxietyScale7.finalScore(50))
    }

    @Test
    fun check566_8() {
        val agree = setOf(
            15, 16, 21, 22, 24, 32, 33, 35, 38, 40, 41, 47, 52, 76,
            97, 104, 121, 156, 157, 159, 168, 179, 182, 194, 202, 210, 212,
            238, 241, 251, 259, 266, 273, 282, 291, 297, 301, 303, 305, 307,
            312, 320, 324, 325, 332, 334, 335, 339, 341, 345, 349, 350, 352,
            354, 355, 356, 360, 363, 364
        )
        val disagree = setOf(
            8, 17, 20, 37, 65, 103, 119, 177, 178, 187,
            192, 196, 220, 276, 281, 306, 309, 322, 330
        )
        val resM = calculateScale(agree, disagree, Gender.Male)
        assertEquals(78 + resM.correctionScaleK.raw, resM.individualismScale8.raw)
        assertEquals(213, resM.individualismScale8.score)

        val resF = calculateScale(agree, disagree, Gender.Female)
        assertEquals(78 + resF.correctionScaleK.raw, resF.individualismScale8.raw)
        assertEquals(178, resF.individualismScale8.score)

        assertEquals(26, scalesM.individualismScale8.finalScore(10))
        assertEquals(103, scalesM.individualismScale8.finalScore(50))

        assertEquals(99, scalesF.individualismScale8.finalScore(55))
        assertEquals(30, scalesF.individualismScale8.finalScore(10))
    }

    @Test
    fun check566_9() {
        val agree = setOf(
            11, 13, 21, 22, 59, 64, 73, 97, 100, 109, 127, 134, 143,
            156, 157, 167, 181, 194, 212, 222, 226, 228, 232, 233, 238, 240,
            250, 251, 263, 266, 268, 271, 277, 279, 298
        )
        val disagree = setOf(
            101, 105, 111, 119, 120, 148, 166, 171, 180, 267, 289
        )
        val resM = calculateScale(agree, disagree, Gender.Male)
        assertEquals(46 + (0.2 * resM.correctionScaleK.raw).roundToInt(), resM.optimismScale9.raw)

        val resF = calculateScale(agree, disagree, Gender.Female)
        assertEquals(46 + (0.2 * resF.correctionScaleK.raw).roundToInt(), resF.optimismScale9.raw)

        assertEquals(26, scalesM.individualismScale8.finalScore(10))
        assertEquals(103, scalesM.individualismScale8.finalScore(50))

        assertEquals(99, scalesF.individualismScale8.finalScore(55))
        assertEquals(30, scalesF.individualismScale8.finalScore(10))
    }

    @Test
    fun check566_0() {
        val agree = setOf(
            32, 67, 82, 111, 117, 124, 138, 147, 171, 172, 180, 201, 236, 267,
            278, 292, 304, 316, 321, 332, 336, 342, 357, 377, 383, 398, 411,
            427, 436, 455, 473, 487, 549, 564
        )
        val disagree = setOf(
            25, 33, 57, 91, 99, 119, 126, 143, 193, 208, 229, 231, 254, 262,
            281, 296, 309, 353, 359, 371, 391, 400, 415, 440, 446, 449, 450,
            451, 462, 469, 479, 481, 482, 505, 521, 547
        )
        val resM = calculateScale(agree, disagree, Gender.Male)
        assertEquals(70, resM.introversionScale0.raw)

        val resF = calculateScale(agree, disagree, Gender.Female)
        assertEquals(70, resF.introversionScale0.raw)

        assertEquals(34, scalesM.introversionScale0.finalScore(10))
        assertEquals(92, scalesM.introversionScale0.finalScore(65))

        assertEquals(39, scalesF.introversionScale0.finalScore(15))
        assertEquals(92, scalesF.introversionScale0.finalScore(65))
    }

    @Test
    fun check566_L() {
        val agree = setOf<Int>()
        val disagree = setOf(
            15, 30, 45, 60, 75, 90, 105, 120, 135, 150, 165, 195, 225, 255, 285
        )
        val resM = calculateScale(agree, disagree, Gender.Male)
        assertEquals(15, resM.liesScaleL.raw)

        val resF = calculateScale(agree, disagree, Gender.Female)
        assertEquals(15, resF.liesScaleL.raw)


        assertEquals(36, scalesM.liesScaleL.finalScore(0))
        assertEquals(70, scalesM.liesScaleL.finalScore(10))

        assertEquals(37, scalesF.liesScaleL.finalScore(0))
        assertEquals(70, scalesF.liesScaleL.finalScore(10))
    }

    @Test
    fun check566_F() {
        val agree = setOf(
            14, 23, 27, 31, 34, 35, 40, 42, 48, 49, 50, 53, 56, 66, 85, 121, 123,
            139, 146, 151, 156, 168, 184, 197, 200, 202, 205, 206, 209, 210, 211, 215, 218,
            227, 245, 246, 247, 252, 256, 269, 275, 286, 291, 293
        )
        val disagree = setOf(
            17, 20, 54, 65, 75, 83, 112, 113, 115, 164, 169, 177, 185, 196,
            199, 220, 257, 258, 272, 276
        )
        val resM = calculateScale(agree, disagree, Gender.Male)
        assertEquals(64, resM.credibilityScaleF.raw)

        val resF = calculateScale(agree, disagree, Gender.Female)
        assertEquals(64, resF.credibilityScaleF.raw)


        assertEquals(44, scalesM.credibilityScaleF.finalScore(0))
        assertEquals(77, scalesM.credibilityScaleF.finalScore(15))

        assertEquals(55, scalesF.credibilityScaleF.finalScore(5))
        assertEquals(78, scalesF.credibilityScaleF.finalScore(15))
    }
}

private fun scaleSum(scale: Scale) = scale.yes.size + scale.no.size

private fun calculateScale(
    agree: Set<Int>,
    disagree: Set<Int>,
    gender: Gender = Gender.Male
): MmpiProcess.Result {
    val size = 566
    val answers = answers(size, agree, disagree)
    val test = MmpiProcess(gender, Type.Mmpi566)

    answers.forEachIndexed { index, answer ->
        test.submitAnswer(index, answer)
    }
    return test.calculateResult()
}
