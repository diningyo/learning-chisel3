// See LICENSE for license details.

import scala.io.Source
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import firrtl.annotations.MemoryLoadFileType

/**
  * Memのテストユニット
  * @param c Memのインスタンス
  */
class MemUnitTester(c: Memory) extends PeekPokeTester(c) {
  /**
    * メモリ・ライト
    * @param addr メモリのワードアドレス
    * @param data 書き込むデータ
    */
  def write(addr: BigInt, data: BigInt): Unit = {
    poke(c.io.wren, true)
    poke(c.io.addr, addr)
    poke(c.io.wrData, data)
    step(1)
  }

  /**
    * メモリ・リード
    * @param addr メモリのワードアドレス
    * @return リードしたメモリのデータ
    */
  def read(addr: BigInt): BigInt = {
    poke(c.io.addr, addr)
    step(1)
    peek(c.io.rdData)
  }

  /**
    * 期待値比較付きメモリ・リード
    * @param addr メモリのワードアドレス
    * @param exp 期待値
    * @return リードの比較結果
    */
  def readCmp(addr: Int, exp: BigInt): Boolean = {
    read(addr)
    expect(c.io.rdData, exp)
  }

  /**
    * テスト本体
    */

  // 初期チェックを実行
  Source.fromFile(c.loadFilePath).getLines.zipWithIndex.foreach {
    case (line, lineNo) => {
      readCmp(lineNo, BigInt(line, 16))
    }
  }

  // ライトしてリード
  Range(0, 15).foreach {
    addr => {
      val wrData = addr + 1
      write(addr,  wrData)
      readCmp(addr,wrData)
    }
  }
}

/**
  * Memのテストクラス
  */
class MemoryTester extends ChiselFlatSpec {

  behavior of "Mem"

  val defaultArgs = Array(
    "--generate-vcd-output", "off",
    "--backend-name", "treadle"
    //,"--is-verbose"
  )

  val loadFileDir = "subprj/load-chisel-mem/src/test/resources/"

  it should "16進文字列のメモリデータが入ったファイルをロード出来る" in {
    val filePath = loadFileDir + "test00_basic.hex"

    Driver.execute(defaultArgs, () => new Memory(filePath)) {
      c => new MemUnitTester(c)
    } should be (true)
  }

  // 期待値不一致を起こすことを確認
  it should "ファイル中の要素数がメモリサイズを超えると、先頭から上書きされる" in {
    val filePath = loadFileDir + "test01_oversize.hex"

    Driver.execute(defaultArgs, () => new Memory(filePath)) {
      c => new MemUnitTester(c)
    } should be (true)
  }

  // 例外はいて落ちる - バックエンドの実行例外
  it should "ファイルデータは16進文字列でのみ構成される" in {
    val filePath = loadFileDir + "test02_illegal.hex"

    Driver.execute(defaultArgs, () => new Memory(filePath)) {
      c => new MemUnitTester(c)
    } should be (true)
  }

  // そのまんまHEXとして扱われる。
  it should "ファイルタイプ == Binaryもあるけど動きが違う気がする" in {
    val filePath = loadFileDir + "test03.bin"

    Driver.execute(defaultArgs, () => new Memory(filePath, MemoryLoadFileType.Binary)) {
      c => new MemUnitTester(c)
    } should be(true)
  }
}
