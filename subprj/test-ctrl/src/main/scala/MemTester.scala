
import scala.language.reflectiveCalls
import chisel3._
import chisel3.iotesters.PeekPokeTester
import chisel3.util._

class MemIO(numOfWords: Int, dataBits: Int) extends Bundle {
  val addr = Input(UInt(log2Ceil(numOfWords).W))
  val wren = Input(Bool())
  val wrdata = Input(UInt(dataBits.W))
  val rddata = Output(UInt(dataBits.W))

  override def cloneType: this.type = new MemIO(numOfWords, dataBits).asInstanceOf[this.type ]
}

class Op extends Bundle {
  val cmd = UInt(2.W)
  val addr = UInt(32.W)
  val data = UInt(32.W)

  def init(req_cmd: Int, req_addr: Int, req_data: Int): this.type = {
    cmd := req_cmd.U
    addr := req_addr.U
    data := req_data.U
    this
  }
}

object Op {
  def idle: Int = 0
  def wr: Int = 1
  def rd: Int = 2
}

class MemTester(numOfWords: Int, dataBits: Int, ops: Seq[(Int, Int, Int)]) extends Module {
  val io = IO(new Bundle {
    val a = Flipped(new MemIO(numOfWords, dataBits))
    val fin = Output(Bool())
  })

  val sIDLE :: sWR :: sRD :: sFin :: Nil = Enum(4)

  val s = ops.map {
    op =>
      val w = Wire(new Op)
      w.init(op._1, op._2, op._3)
      w
  }
  val v = VecInit(s)
  val cmd_q = RegInit(v)
  val q_rd_ptr = RegInit(0.U(log2Ceil(cmd_q.length).W))
  val next_op = cmd_q(q_rd_ptr)

  val r_stm = RegInit(sIDLE)

  val rden = RegInit(false.B)
  val rddv = RegNext(rden, false.B)

  rden := Mux(next_op.cmd === Op.rd.U, true.B, false.B)
  val rd_exp = RegNext(next_op.data)

  when (r_stm === sRD) {
    rddv := true.B
  } .otherwise {
    rddv := false.B
  }

  switch (r_stm) {
    is (sIDLE) {
      q_rd_ptr := q_rd_ptr + 1.U
      when (next_op.cmd === Op.wr.U) {
        r_stm := sWR
      }

      when (next_op.cmd === Op.rd.U) {
        r_stm := sRD
      }
    }

    is (sWR) {
      q_rd_ptr := q_rd_ptr + 1.U
      when (next_op.cmd === Op.idle.U) {
        r_stm := sWR
      }

      when (next_op.cmd === Op.rd.U) {
        r_stm := sWR
      }
    }

    is (sRD) {
      when (next_op.cmd === Op.idle.U) {
        r_stm := sIDLE

      }
      when (next_op.cmd === Op.rd.U) {
        r_stm := sWR
      }
    }
  }

  io.a.wren <> next_op.cmd === Op.wr.U
  io.a.addr := next_op.addr
  io.a.wrdata := next_op.data
  io.fin := r_stm === sFin
}


object Elaborate extends App {

  val ops = Range(0, 10).map{s => (s, s, s)}

  Driver.execute(args, () => new MemTester(256, 32, ops))
}

object Test extends App {

  val ops = Range(0, 10).map{s => (s, s, s)}

  chisel3.iotesters.Driver.execute(Array(
    "--generate-vcd-output=on"
  ),
    () => new MemTester(256, 32, ops)) {
    c => new PeekPokeTester(c) {
      step(1000)
    }
  }
}
