package mmpi

//object CorrectionScaleK : Scale(//30
//    title = "Шкала коррекции",
//    yes = listOf(96),
//    no = listOf(
//        30, 39, 71, 89, 124, 129, 134, 138, 142, 148, 160, 170, 171, 180, 183, 217,
//        234, 267, 272, 296, 316, 322, 374, 383, 397, 398, 406, 461, 502
//    ),
//    costOfZero = 27,
//    costOfKeyAnswer = 2.0f,
//    correctionFactor = 0.0f,
//    tA = 0.3f,
//    tB = - 10.7f
//)
//
//object LiesScaleL : Scale(//15
//    title = "Шкала лжи",
//    yes = emptyList(),
//    no = listOf(15, 30, 45, 60, 75, 90, 105, 120, 135, 150, 165, 195, 225, 255, 285),
//    costOfZero = 37,
//    costOfKeyAnswer = 3.4f,
//    correctionFactor = 0.0f,
//    tA = 0.4f,
//    tB = -8.85f
//)
//
//object CredibilityScaleF : Scale(//64
//    title = "Шкала достоверности",
//    yes = listOf(
//        14, 23, 27, 31, 34, 35, 40, 42, 48, 49, 50, 53, 56, 66, 85, 121, 123, 139,
//        146, 151, 156, 168, 184, 197, 200, 202, 205, 206, 209, 210, 211, 215, 218,
//        227, 245, 246, 247, 252, 256, 269, 275, 286, 291, 293
//    ),
//    no = listOf(
//        17, 20, 54, 65, 75, 83, 112, 113, 115, 164, 169, 177, 185, 196, 199, 220,
//        257, 258, 272, 276
//    ),
//    costOfZero = 44,
//    costOfKeyAnswer = 2.2f,
//    correctionFactor = 0.0f,
//    tA = 0.54f,
//    tB = -15f
//)
//
//object IntroversionScale0 : Scale(//70
//    title = "Шкала интроверсии",
//    yes = listOf(
//        32, 67, 82, 111, 117, 124, 138, 147, 171, 172, 180, 201, 236, 267,
//        278, 292, 304, 316, 321, 332, 336, 342, 357, 377, 383, 398, 411, 427,
//        436, 455, 473, 487, 549, 564
//    ),
//    no = listOf(
//        25, 33, 57, 91, 99, 119, 126, 143, 193, 208, 229, 231, 254, 262, 281, 296,
//        309, 353, 359, 371, 391, 400, 415, 440, 446, 449, 450, 451, 462, 469, 479,
//        481, 482, 505, 521, 547
//    ),
//    costOfZero = 24,
//    costOfKeyAnswer = 1f,
//    correctionFactor = 0f,
//    tA = 1f,
//    tB = 23.74f
//)
//
//object OverControlScale1 : Scale(//33
//    title = "Шкала гиперконтроля",
//    yes = listOf(23, 29, 43, 62, 72, 108, 114, 125, 161, 189, 273),
//    no = listOf(
//        2, 3, 7, 9, 18, 51, 55, 63, 68, 103, 130, 153, 155, 163, 175, 188, 190, 192,
//        230, 243, 274, 281
//    ),
//    costOfZero = 21,
//    costOfKeyAnswer = 2.57f,
//    correctionFactor = 0.5f,
//    tA = 0.4f,
//    tB = -8.65f
//)
//
//object PassivityScale2 : Scale( //60
//    title = "Шкала пассивности",
//    yes = listOf(
//        5, 13, 23, 32, 41, 43, 52, 67, 86, 104, 130, 138, 142, 158, 159, 182, 189,
//        193, 236, 259
//    ),
//    no = listOf(
//        2, 8, 9, 18, 30, 36, 39, 46, 51, 57, 58, 64, 80, 88, 89, 95, 98, 107, 122,
//        131, 145, 152, 153, 154, 155, 160, 178, 191, 207, 208, 233, 241, 242, 248,
//        263, 270, 271, 272, 285, 296
//    ),
//    costOfZero = 16,
//    costOfKeyAnswer = 1.9f,
//    correctionFactor = 0.0f,
//    tA = 0.41f,
//    tB = -3.9f
//)
//
//object LabilityScale3 : Scale(//60
//    title = "Шкала демонстративности",
//    yes = listOf(
//        10, 23, 32, 43, 44, 47, 76, 114, 179, 186, 189, 238, 253
//    ),
//    no = listOf(
//        2, 3, 6, 7, 8, 9, 12, 26, 30, 51, 55, 71, 89, 93, 103, 107, 109, 124, 128,
//        129, 136, 137, 141, 147, 153, 160, 162, 163, 170, 172, 174, 175, 180, 188,
//        190, 192, 201, 213, 230, 234, 243, 265, 267, 274, 279, 289, 292
//    ),
//    costOfZero = 16,
//    costOfKeyAnswer = 1.9f,
//    correctionFactor = 0.0f,
//    tA = 0.55f,
//    tB = -11.5f
//)
//
//object ImpulsivenessScale4 : Scale(//50
//    title = "Шкала импульсивности",
//    yes = listOf(
//        16, 21, 24, 32, 33, 35, 38, 42, 61, 67, 84, 94, 102, 106, 110, 118, 127,
//        215, 216, 224, 239, 244, 245, 284
//    ),
//    no = listOf(
//        8, 20, 37, 82, 91, 96, 107, 134, 137, 141, 155, 170, 171, 173, 180, 183,
//        201, 231, 235, 237, 248, 267, 287, 289, 294, 296
//    ),
//    costOfZero = 16,
//    costOfKeyAnswer = 1.9f,
//    correctionFactor = 0.4f,
//    tA = 0.42f,
//    tB = -2.76f
//)
//
//object MasculinityScale5M : Scale(//60
//    title = "Шкала мужественности",
//    yes = listOf(
//        4, 25, 69, 70, 74, 77, 78, 87, 92, 126, 132, 134, 140, 149, 179, 187, 203,
//        204, 217, 226, 231, 239, 261, 278, 282, 295, 297, 299
//    ),
//    no = listOf(
//        1, 19, 26, 28, 79, 80, 81, 89, 99, 112, 115, 116, 117, 120, 133, 144, 176,
//        198, 213, 214, 219, 221, 223, 229, 249, 254, 260, 262, 264, 280, 283, 300
//    ),
//    costOfZero = 130,
//    costOfKeyAnswer = -1.9f,
//    correctionFactor = 0.0f,
//    tA = 0.5f,
//    tB = -5.3f
//)
//
//object FemininityScale5F : Scale(//60
//    title = "Шкала женственности",
//    yes = listOf(
//        4, 25, 70, 74, 77, 78, 87, 92, 126, 132, 133, 134, 140, 149, 187, 203, 204,
//        217, 226, 239, 261, 278, 282, 295, 299
//    ),
//    no = listOf(
//        1, 19, 26, 28, 69, 79, 80, 81, 89, 99, 112, 115, 116, 117, 120, 144, 176,
//        179, 198, 213, 214, 219, 221, 223, 229, 231, 249, 254, 260, 262, 264, 280,
//        283, 297, 300
//    ),
//    costOfZero = 130,
//    costOfKeyAnswer = -1.9f,
//    correctionFactor = 0.0f,
//    tA = 0.5f,
//    tB = -5.3f
//)
//
//object RigidityScale6 : Scale(//40
//    title = "Шкала ригидности",
//    yes = listOf(
//        15, 16, 22, 24, 27, 35, 110, 121, 123, 127, 151, 157, 158, 202, 275, 284,
//        291, 293, 299, 305, 317, 338, 341, 364, 365
//    ),
//    no = listOf(
//        93, 107, 109, 111, 117, 124, 268, 281, 294, 313, 316, 319, 327, 347, 348
//    ),
//    costOfZero = 28,
//    costOfKeyAnswer = 3f,
//    correctionFactor = 0.0f,
//    tA = 0.37f,
//    tB = -9.72f
//)
//
//object AnxietyScale7 : Scale(//48
//    title = "Шкала тревожности",
//    yes = listOf(
//        10, 15, 22, 32, 41, 67, 76, 86, 94, 102, 106, 142, 159, 182, 189, 217, 238,
//        266, 301, 304, 305, 317, 321, 336, 337, 340, 342, 343, 344, 346, 349, 351,
//        352, 356, 357, 358, 359, 360, 361
//
//    ),
//    no = listOf(
//        3, 8, 36, 122, 152, 164, 178, 329, 353
//    ),
//    costOfZero = 28,
//    costOfKeyAnswer = 3f,
//    correctionFactor = 1.0f,
//    tA = 0.4f,
//    tB = -1.1f
//)
//
//object IndividualismScale8 : Scale(//78
//    title = "Шкала индивидулизма",
//    yes = listOf(
//        15, 16, 21, 22, 24, 32, 33, 35, 38, 40, 41, 47, 52, 76,
//        97, 104, 121, 156, 157, 159, 168, 179, 182, 194, 202, 210, 212,
//        238, 241, 251, 259, 266, 273, 282, 291, 297, 301, 303, 305, 307,
//        312, 320, 324, 325, 332, 334, 335, 339, 341, 345, 349, 350, 352,
//        354, 355, 356, 360, 363, 364
//    ),
//    no = listOf(
//        8, 17, 20, 37, 65, 103, 119, 177, 178, 187, 192, 196, 220, 276,
//        281, 306, 309, 322, 330
//    ),
//    costOfZero = 15,
//    costOfKeyAnswer = 1.5f,
//    correctionFactor = 1.0f,
//    tA = 0.52f,
//    tB = -4f
//)
//
//object OptimismScale9 : Scale(//46
//    title = "Шкала оптимизма",
//    yes = listOf(
//        11, 13, 21, 22, 59, 64, 73, 97, 100, 109, 127, 134, 143,
//        156, 157, 167, 181, 194, 212, 222, 226, 228, 232, 233, 238, 240,
//        250, 251, 263, 266, 268, 271, 277, 279, 298
//    ),
//    no = listOf(
//        101, 105, 111, 119, 120, 148, 166, 171, 180, 267, 289
//    ),
//    costOfZero = 8,
//    costOfKeyAnswer = 2.5f,
//    correctionFactor = 0f,
//    tA = 0.4f,
//    tB = -3.4f
//)