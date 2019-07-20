// See LICENSE for license details.

import chisel3.iotesters._

import scala.math.{floor, random}

/**
  * Mem3Dの単体テストクラス
  * @param c Mem3D
  */
class Mem3DUnitTester(c: Mem3D) extends PeekPokeTester(c) {

  /**
    * メモリライト
    * @param bank 読みだすメモリのバンク
    * @param addr メモリアドレス
    * @param data 書き込むデータ
    */
  def write(bank: Int, addr: Int, data: BigInt): Unit = {
    poke(c.io.wren(bank), true)
    poke(c.io.addr, addr)
    poke(c.io.wrdata(bank), data)
    step(1)
    poke(c.io.wren(bank), false)
  }

  /**
    * メモリリード
    * @param bank 読みだすバンク
    * @param addr メモリアドレス
    * @return メモリの値
    */
  def read(bank: Int, addr: Int): BigInt = {
    poke(c.io.rden, true)
    poke(c.io.addr, addr)
    step(1)
    poke(c.io.rden, false)
    peek(c.io.rddata(bank))
  }

  /**
    * テストシナリオ
    *  - 各バンク毎のアドレス0-15に適当に値書いて読むだけ
    */
  for {i <- 0 until 2
       j <- 0 until 16} {
    val data = i + longToUnsignedBigInt(floor(random * 0xffffffffL).toLong)
    write(i, j, data)
    step(1)
    println(s"read data($i, $j) = 0x${read(i, j).toInt.toHexString}")
    expect(c.io.rddata(i), data)
  }
}

/**
  * Mem3Dのテスト
  */
class Mem3DTester extends ChiselFlatSpec {

  behavior of "Mem3D"

  it should "32bit単位でメモリの各バンクにアクセス出来る" in {
    Driver.execute(Array[String](), () => new Mem3D()) {
      c => new Mem3DUnitTester(c)
    } should be (true)
  }

  it should "writeを使っても32bit単位でメモリの各バンクにアクセス出来る" in {
    Driver.execute(Array[String](), () => new Mem3D(true)) {
      c => new Mem3DUnitTester(c)
    } should be (true)
  }
}
