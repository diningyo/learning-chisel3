
import chisel3._

/*
/**
  * BundleもScalaのクラスなのでメソッドを定義できる
  */
class MyType extends Bundle {
  val a = UInt(8.W)
  val b = UInt(8.W)
  val valid = Bool()

  def decode(v: UInt): Unit = {
    a := v(15, 8)
    b := v(7, 0)
    valid := a === b
  }
}
*/

class MyType extends Bundle {
  val a = UInt(8.W)
  val b = UInt(8.W)

  def decode(v: UInt): Unit = {
    a := v(15, 8)
    b := v(7, 0)
  }

  def valid: Bool = a === b
}

/**
  * Bundleで作ったRegの出力信号を確認するだけのモジュール
  */
class MyModule extends Module {
  //　MyTypeのインスタンスでIOポートを作成
  val io = IO(new Bundle {
    val in = Input(UInt(16.W))
    val out = Output(new MyType)
  })

  val myTypeReg = Reg(new MyType)   // MyTypeでRegを作成

  when (myTypeReg.valid) {
    myTypeReg.decode(io.in)
  }
  io.out := myTypeReg
}

/**
  * エラボレート
  */
object Elaborate extends App {
  Driver.execute(Array(
    "-tn=MyModule", "-td=rtl"
  ), () => new MyModule)
}