// See LICENSE for license details.

import chisel3._

/**
  * RegInit用のBundleサンプルその１
  */
class BundleRegInit0 extends Bundle {
  val a = UInt(4.W)
  val b = UInt(4.W)
}

/**
  * Bundleで作るRegInitの各フィールドを任意の値で初期化
  */
class SampleBundleRegInit0 extends Module {

  val io = IO(new Bundle {
    val en = Input(Bool())
    val data = Input(UInt(8.W))
    val out = Output(new BundleRegInit0)
  })

  val bundleObj = Wire(new BundleRegInit0) // Wireでラップする

  // 初期値を設定
  bundleObj.a := 1.U
  bundleObj.b := 2.U

  // 初期値設定済みのWireで初期化してレジスタを作成
  val out = RegInit(bundleObj)

  when (io.en) {
    out.a := io.data
    out.b := io.data
  }

  io.out <> out
}
