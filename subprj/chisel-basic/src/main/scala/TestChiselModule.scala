// See LICENSE for license details.

import chisel3._

/**
  * Chiselのモジュール基本形
  * このモジュールは入力"in"を出力"out"に接続しスルーするだけ
  */
class TestChiselModule extends Module {
  val io = IO(new Bundle {
    val in = Input(Bool())
    val out = Output(Bool())
  })

  // 信号の接続
  io.out := io.in
}
