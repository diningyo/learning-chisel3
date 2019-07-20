// See LICENSE for license details.

package fifo

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}


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

class FifoTester extends ChiselFlatSpec {

  val args = Array(
    "--generate-vcd-output", "on",
    "--backend-name", "verilator",
    "--target-dir", "test_run_dir/fifo_test",
    "--top-name", "fifo_dtm"
    //"--is-verbose"
  )
  val dataBits = 32
  val fifoStage = 16

  "Fifo" should "wrenを0x1にするとその時のwrDataがFIFOに書き込まれ、countが1増える" in {
    Driver.execute(args, () => new Fifo(dataBits, fifoStage)) {
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

  it should "rdenを0x1にするとFIFO内部の次のデータがrdDataに出力され、countが１減る" in {
    Driver.execute(args, () => new Fifo(dataBits, fifoStage)) {
      c => new FifoUnitTester(c) {
        val testVal = List(BigInt("deadbeaf", 16), BigInt("a0a0a0a0", 16))

        // リセット後の端子の確認
        reset()

        // テストデータをセット
        setListData(testVal)

        // popの試験
        testVal.zipWithIndex.foreach {
          case(exp: BigInt, i: Int) => {
            val expCount = testVal.length - i
            val expEmpty = expCount == 0
            expectOutput(
              expEmpty, expFull = false, expCount = expCount, expRdData = exp)
            pop()
          }
        }

        step(1)

        expectOutput(
          expEmpty = true, expFull = false, expCount = 0x0, expRdData = BigInt("0", 16))
      }
    } should be(true)
  }

  it should "wrenとrdenが同時に0x1になるとcountはそのままでrdDataが更新される" in {
    Driver.execute(args, () => new Fifo(dataBits, fifoStage)) {
      c => new FifoUnitTester(c) {
        val testVal = List(BigInt("deadbeaf", 16), BigInt("a0a0a0a0", 16))
        val hasData = true
        val expCount = 1

        reset()
        push(testVal(0))
        expectOutput(
          expEmpty = false, expFull = false, expCount = 1, expRdData = testVal(0))

        pushAndPop(testVal(1))
        expectOutput(
          expEmpty = false, expFull = false, expCount = 1, expRdData = testVal(1))
      }
    } should be(true)
  }

  it should "emptyはFIFO内部にデータがある場合にLowになる" in {
    Driver.execute(args, () => new Fifo(dataBits, fifoStage)) {
      c => new FifoUnitTester(c) {
        reset()
        expect(c.io.empty, true)
        for (i <- 0 until fifoStage) {
          push(i + 1)
          expect(c.io.empty, false)
        }
        step(1)
      }
    } should be(true)
  }

  it should "fullはFIFO内部の全バッファにデータが入るとHighになり、それ以外はLowのままになる。" in {
    Driver.execute(args, () => new Fifo(dataBits, fifoStage)) {
      c => new FifoUnitTester(c) {
        reset()
        for (i <- 0 until fifoStage) {
          expect(c.io.full, false)
          push(i + 1)
        }
        step(1)
        expect(c.io.full, true)
        step(1)
      }
    } should be(true)
  }

  it should "データのビット幅を可変に出来る（0 < dataBits）" in {
    for (i <- 1 to 32) {
      println(s"dataBits = $i")
      Driver.execute(args, () => new Fifo(i, dataBits)) {
        c => new FifoUnitTester(c) {
          val wrData = BigInt("1") << (i - 1)
          reset()
          push(wrData)
          expect(c.io.rdData, wrData)
          step(1)
        }
      } should be(true)
    }
  }

  it should "FIFO内部のバッファ段数を可変に出来ること（0 < fifoStage）" in {
    for (i <- 1 to 32) {
      println(s"fifoStage = $i")
      Driver.execute(args, () => new Fifo(dataBits, i)) {
        c => new FifoUnitTester(c) {
          reset()
          for (j <- 0 until i) {
            expect(c.io.full, false)
            push(j + 1)
          }
          step(1)
          expect(c.io.full, true)
          step(1)
        }
      } should be(true)
    }
  }
}
