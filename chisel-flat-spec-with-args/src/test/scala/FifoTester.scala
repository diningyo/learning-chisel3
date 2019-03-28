// See LICENSE for license details.

package fifo

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import org.scalatest.{BeforeAndAfterAllConfigMap, ConfigMap}


class FifoUnitTester(c: Fifo) extends PeekPokeTester(c) {
  /**
    * Fifoにデータをセット
    * @param wrData: 設定するデータ
    */
  def push(wrData: BigInt): Unit = {
    poke(c.io.wren, true)
    poke(c.io.wrData, wrData)
    step(1)
    poke(c.io.wren, false)
  }

  /**
    * Fifoのデータを読み取る
    * @return Fifoの出力データ
    */
  def pop(): BigInt = {
    val rdData = peek(c.io.rdData) // 現在のリードデータを取得
    poke(c.io.rden, true)    // rdDataを更新
    step(1)
    poke(c.io.rden, false)
    rdData
  }

  /**
    * Fifoにデータをセットしつつデータを読み取る
    * @param wrData: 設定するデータ
    * @return Fifoの出力データ
    */
  def pushAndPop(wrData: BigInt): BigInt = {
    poke(c.io.wren, true)
    poke(c.io.wrData, wrData)

    val rdData = peek(c.io.rdData) // 現在のリードデータを取得
    poke(c.io.rden, true)    // rdDataを更新
    step(1)
    poke(c.io.wren, false)
    poke(c.io.rden, false)
    rdData
  }

  /**
    * 出力ポートの期待値を比較
    * @param expEmpty io.emptyの期待値
    * @param expFull io.fullの期待値
    * @param expCount io.countの期待値
    * @param expRdData io.rdDataの期待値
    * @return
    */
  def expectOutput(
                    expEmpty: Boolean,
                    expFull: Boolean,
                    expCount: Int,
                    expRdData: BigInt): Boolean = {
    expect(c.io.empty, expEmpty)
    expect(c.io.full, expFull)
    expect(c.io.count, expCount)
    expect(c.io.rdData, expRdData)
  }

  /**
    * Fifoにデータをセット
    * @param data Fifoにセットするデータ
    */
  def setListData(data: List[BigInt]): Unit = data.foreach { d => push(d) }
}

class FifoTester extends ChiselFlatSpec with BeforeAndAfterAllConfigMap {

  val defaultArgs = scala.collection.mutable.Map(
    "--generate-vcd-output" -> "on",
    "--backend-name" -> "verilator"
  )

  override def beforeAll(configMap: ConfigMap): Unit = {
    defaultArgs.foreach {
      case (key, value) => {
        if (configMap.get(key).isDefined) {
          defaultArgs(key) = value
          println(s"(key, value) = ($key, $value)")
        }
      }
    }
  }

  def makeArgs(): Array[String] = {
    defaultArgs.map {
      case (key: String, value: String) => (s"$key=$value")
    }.toArray
  }

  val dataBits = 32
  val fifoStage = 16

  "Fifo" should "wrenを0x1にするとその時のwrDataがFIFOに書き込まれ、countが1増える" in {
    Driver.execute(makeArgs(), () => new Fifo(dataBits, fifoStage)) {
      c => new FifoUnitTester(c) {
        val testVal = List(BigInt("deadbeaf", 16), BigInt("a0a0a0a0", 16))

        // リセット後の状態確認
        reset()
        expectOutput(
          expEmpty = true, expFull = false, expCount = 0, expRdData = BigInt(0))

        testVal.zipWithIndex.foreach {
          case(d: BigInt, i: Int) => {
            push(d)
            expectOutput(
              expEmpty = false, expFull = false, expCount = i + 1, expRdData = testVal(0))
          }
        }
        step(1)
        expectOutput(
          expEmpty = false, expFull = false, expCount = testVal.length, expRdData = testVal(0))
      }
    } should be(true)
  }
}
