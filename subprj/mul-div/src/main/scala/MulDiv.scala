
import chisel3._
import chisel3.util._


sealed trait MulType
case object MulSeq extends MulType
case object MulArray extends MulType
case object MulWallaceTree extends MulType
case object Mul4to2Tree extends MulType



class Mul(bits: Int, mulType: MulType) extends Module {

  val io = IO(new Bundle {
    val a = Input(UInt(bits.W))
    val b = Input(UInt(bits.W))
    val c = Output(UInt((bits * 2).W))
  })

  def mulSeq(a: UInt, b: UInt): UInt = {

    val bitWidth = a.getWidth
    val partial_mul = Seq.fill(bitWidth)(Wire(UInt(bitWidth.W)))

    for (bit_idx <- 0 until bitWidth) {
      val b = Cat(VecInit(Seq.fill(bitWidth)(io.b(bit_idx))))
      partial_mul(bit_idx) := a & b
    }

    partial_mul.zipWithIndex.map { case (a:UInt, i:Int) => a << i.U }.reduce((a, b)  =>  a +& b)
    /*
    val (result, _)  = partial_mul.zipWithIndex.map {
      case (a:UInt, i:Int) => a << i.U
    }.zipWithIndex.reduce {
      (a, b) => {
        val r = a._1 +& b._1
        val ret: UInt = if (a._2 % 4 == 0) RegNext(r) else WireInit(r)
        (ret, a._2)
      }
    }

    result
    */
  }

  val result = mulType match {
    case _: MulSeq.type => mulSeq(io.a, io.b)
    case _ => 0.U
  }

  io.c := result
}

object MulTest extends App {
  import chisel3.iotesters.{Driver, PeekPokeTester}

  Driver.execute(args, () => new Mul(16, MulSeq)) {
  //Driver.execute(args :+ "-tiv" :+ "-tgvo=on", () => new Mul(4, MulSeq)) {
    c => new PeekPokeTester[Mul](c) {
      for (a <- 0 until 65536; b <- 0 until 65536) {
        if (b == 0) println(s"a = $a")
        poke(c.io.a, a)
        poke(c.io.b, b)
        expect(c.io.c, a * b)
        step(1)
      }
    }
  }
}

object MulElaborate extends App {
  Driver.execute(args :+  "-tn=MulSeq" :+ "-td=rtl", () => new Mul(16, MulSeq))
}

class Div(bits: Int) extends Module {

  val io = IO(new Bundle {
    val a = Input(UInt(bits.W))
    val b = Input(UInt(bits.W))
    val c = Output(UInt((bits * 2).W))
  })


}
