// See LICENSE for license details.

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class XorShiftUnitTester(c: XorShift32Fixed) extends PeekPokeTester(c) {

  /**
    * XorShift32の期待値生成
    * @param seed シード
    * @return XorShift32で生成した乱数値
    */
  def xorshift32(seed: BigInt): BigInt = {
    var y = seed
    y = y ^ (y << 13) & BigInt("ffffffff", 16)
    y = y ^ (y >> 17) & BigInt("ffffffff", 16)
    y = y ^ (y << 5)  & BigInt("ffffffff", 16)
    y & BigInt("ffffffff", 16)
  }

}

class XorShiftTester extends ChiselFlatSpec {

  behavior of "XorShift"

  val defaultArgs: Array[String] = Array("--generate-vcd-output=on")

  it should "updateがHighの時には毎サイクル乱数が生成される" in {
    Driver.execute(defaultArgs, () => new XorShift32Fixed(BigInt(1), List(0, 1024))) {
      c => new XorShiftUnitTester(c) {
        reset()

        var exp = BigInt(1)
        for (i <- 0 until 10) {
          exp = xorshift32(exp)
          println(s"(i, exp) = ($i, 0x${exp.toInt.toHexString})")
          poke(c.io.update, true)
          step(1)
          expect(c.io.randOut, exp & BigInt("3ff", 16))
        }
      }
    } should be (true)
  }
}
