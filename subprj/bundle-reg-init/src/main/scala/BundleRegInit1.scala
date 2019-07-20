// See LICENSE for license details.

import chisel3._

/**
  * RegInit用のBundleサンプルその２
  */
class BundleRegInit1 extends Bundle {
  val a = UInt(4.W)
  val b = UInt(4.W)

  /**
    * 初期化用メソッド
    * @return 初期化後のインスタンス
    */
  def init(): BundleRegInit1 = {
    a := 2.U
    b := 3.U
    this
  }
}

/**
  * Bundleで作るRegInitの各フィールドを任意の値で初期化
  */
class SampleBundleRegInit1 extends Module {

  val io = IO(new Bundle {
    val en = Input(Bool())
    val data = Input(UInt(8.W))
    val out = Output(new BundleRegInit1)
  })

  val out = RegInit(Wire(new BundleRegInit1).init())

  when (io.en) {
    out.a := io.data
    out.b := io.data
  }

  io.out <> out
}
