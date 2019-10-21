
import chisel3._
import chisel3.util._

/**
 * MixedVecでパラメタライズしたBundleのインスタンスを生成できるかを確認
 * @param bits addrのビット幅
 */
class A(bits: Int) extends Bundle {
  val addr = UInt(bits.W)

  override def cloneType:this.type = new A(bits).asInstanceOf[this.type]
}

/**
 * MixedVecの使い方の確認コード
 * @param memConfig メモリマップの設定（ただ単にアドレスのビット幅をIntで持っているだけ）
 */
class TrialMixedVec(memConfig: Seq[Int]) extends Module {

  val io = IO(new Bundle {
    val x = Input(UInt(3.W))
    val y = Input(UInt(10.W))
    val vec = Output(MixedVec(memConfig.map(new A(_))))
  })
  io.vec(0).addr := io.x
  io.vec(1).addr := io.y
}

/**
 * RTLの生成
 */
object ElaborateMixedVec extends App {
  val memConfig = Seq(4, 15)

  Driver.execute(
    Array(
      "-td=rtl/chisel-3.2.0/TrialMixedVec"
    ), () => new TrialMixedVec(memConfig))
}