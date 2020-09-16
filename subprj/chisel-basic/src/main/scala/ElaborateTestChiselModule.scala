// See LICENSE for license details.

import chisel3._

/**
  * TestChiselModuleのエラボレート
  */
object ElaborateTestChiselModule {

  /**
    * Scalaのメイン関数
    * @param args プログラム引数
    */
  def main(args: Array[String]) {
    Driver.execute(Array(
      "--target-dir=rtl/chisel-basic"
    ),
    () => new TestChiselModule
    )
  }
}

/**
  * TestChiselModuleのエラボレートのAppトレイト使用版
  * Appトレイトを継承することでmainメソッドを作らなくて済む
  */
object ElaborateTestChiselModuleApp extends App{

  Driver.execute(Array(
      "--target-dir=rtl/chisel-basic"
    ),
    () => new TestChiselModule
    )
}
