// See LICENSE for license details.

import chisel3._

/**
  * RegInit用のBundleサンプルその３
  */
class BundleRegInit2 extends Bundle {
  val a = UInt(4.W)
  val b = UInt(4.W)

  /**
    * 初期値を設定する
    * @return 初期化後の自クラス
    */
  def init(valA: Int = 3, valB: Int = 4): BundleRegInit2 = {
    a := valA.U
    b := valB.U
    this
  }

  /**
    * ライト
    * @param data 書き込む値
    */
  def write(data: UInt): Unit = {
    a := data(3, 0)
    b := data(7, 4)
  }
}

/**
  * class BundleRegInit2 のコンパニオン・オブジェクト
  */
object BundleRegInit2 {
  /**
    * initを呼んで初期値を設定
    * @return 設定後のインスタンス
    */
  def apply(): BundleRegInit2 = {
    val m = Wire(new BundleRegInit2())
    m.init()
  }

  /**
    * 生成時に引数の値で初期化
    * @param data 初期化時に設定する値
    * @return 設定後のインスタンス
    */
  def apply(data: UInt): BundleRegInit2 = {
    val m = Wire(new BundleRegInit2())
    m.write(data)
    m
  }
}

/**
  * Bundleで作るRegInitの各フィールドを任意の値で初期化
  */
class SampleBundleRegInit2 extends Module {

  val io = IO(new Bundle {
    val en = Input(Bool())
    val data = Input(UInt(32.W))
    val out1 = Output(new BundleRegInit0)
    val out2 = Output(new BundleRegInit2)
  })

  val out1 = RegInit(BundleRegInit2())
  val out2 = RegInit(BundleRegInit2("h77".U))

  when (io.en) {
    out1.write(io.data)
    out2.write(io.data)
  }

  io.out1 <> out1
  io.out2 <> out2
}
