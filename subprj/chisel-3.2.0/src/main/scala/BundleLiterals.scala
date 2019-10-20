
import chisel3._
import chisel3.util._
import chisel3.experimental.BundleLiterals._
import chisel3.experimental.BundleLiteralException

class BundleLiterals extends Bundle {
  val bool = Bool()
  val uint = UInt(8.W)
  val bundle = new Bundle {
    val a = Bool()
  }
}

class UseBundleLiterals extends Module {
  val io = IO(Output(new BundleLiterals))

  // 以下のように.Lit()内で初期化が出来る
  val out = new BundleLiterals().Lit(
    _.bool -> true.B,
    _.uint -> 0xff.U,
    _.bundle.a -> true.B
  )

  io := out
}

object ElaborateBundleLiterals extends App {
  Driver.execute(args, () => new UseBundleLiterals)
}