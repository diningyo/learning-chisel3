
import chisel3._
import chisel3.util._

class A(bits: Int) extends Bundle {
  val addr = UInt(bits.W)

  override def cloneType:this.type = new A(bits).asInstanceOf[this.type]
}


class TrialMixedVec(memConfig: Seq[Int]) extends Module {

  val io = IO(new Bundle {
    val x = Input(UInt(3.W))
    val y = Input(UInt(10.W))
    val vec = Output(MixedVec(memConfig.map(new A(_))))
  })
  io.vec(0).addr := io.x
  io.vec(1).addr := io.y
}

object ElaborateMixedVec extends App {
  val memConfig = Seq(4, 15)

  Driver.execute(args, () => new TrialMixedVec(memConfig))
}