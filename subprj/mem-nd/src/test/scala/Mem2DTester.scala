// See LICENSE for license details.

import chisel3.iotesters._

/**
  * Mem2Dの単体テストクラス
  * @param c Mem2D
  */
class Mem2DUnitTester(c: Mem2D) extends PeekPokeTester(c) {
  /**
    * メモリライト
    * @param addr メモリアドレス
    * @param data 書き込むデータ
    */
  def write(addr: Int, data: BigInt): Unit = {
    poke(c.io.wren, true)
    poke(c.io.addr, addr)
    poke(c.io.wrdata, data)
    step(1)
    poke(c.io.wren, false)
  }

  /**
    * メモリリード
    * @param addr メモリアドレス
    * @return メモリの値
    */
  def read(addr: Int): BigInt = {
    poke(c.io.rden, true)
    poke(c.io.addr, addr)
    step(1)
    poke(c.io.rden, false)
    peek(c.io.rddata)
  }

  import scala.math.{floor, random}
  /**
    * テストシナリオ
    *  - アドレス0-15に適当に値書いて読むだけ
    */
  for (i <- 0 until 16) {
    val data = i + longToUnsignedBigInt(floor(random * 0xffffffffL).toLong)
    write(i, data)
    step(1)
    println(s"read data = 0x${read(i).toInt.toHexString}")
    expect(c.io.rddata, data)
  }
}

/**
  * Mem2Dのテスト
  */
class Mem2DTester extends ChiselFlatSpec {

  behavior of "Mem2D"

  it should "32bit単位でメモリにアクセス出来る" in {
    Driver.execute(Array[String]("--generate-vcd-output=on"), () => new Mem2D) {
      c => new Mem2DUnitTester(c)
    } should be (true)
  }

  it should "Memのwriteを使っても32bit単位でメモリにアクセス出来る" in {
    Driver.execute(Array[String](), () => new Mem2D(true)) {
      c => new Mem2DUnitTester(c)
    } should be (true)
  }
}
