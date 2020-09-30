
import chisel3._
import chisel3.util._

object ExpandToFixedPoint extends App {
  val bd = BigDecimal("1.125")
  val bdLit = bd.I(3.BP) // .IでBiGDecimalをFixedPointに変換

  // FixedPointをBigDecimalに変換してbdを比較
  println(s"${bdLit.litToBigDecimal == bd}")

  val d = 1.125
  val dLit = d.F(3.BP) // .FでDoubleをFixePointに変換

  // FixedPointをDoubleに変換してdを比較
  println(s"${dLit.litToDouble == d}")
}
