// See LICENSE for license details.

import chisel3._
import chisel3.iotesters._

/**
  * TestChiselTypesのエラボレート
  */
object ElaborateTestChiselTypes extends App{

  /*
   * 各Chiselのデータ型に設定された値を確認するために
   * テスト用のDriverを使用する
   */
  iotesters.Driver.execute(Array(
    "--target-dir=test_run_dir/chisel-basic",
    "--top-name=TestChiselTypes"
  ),
  () => new TestChiselTypes) {
    c => new PeekPokeTester(c) {
      step(1)
    }
  }

  // RTL生成用
  chisel3.Driver.execute(Array(
    "--target-dir=rtl/chisel-basic"
  ),
    () => new TestChiselTypes
  )
}
