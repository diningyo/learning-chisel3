// See README.md for license details.

import chisel3._
import chisel3.util._
import chisel3.experimental.IntParam

class MyData(dataBits: Int) extends Bundle {
  val strb = UInt((dataBits / 8).W)
  val data = UInt(dataBits.W)
  val last = Bool()

  override def cloneType: this.type =
    new MyData(dataBits).asInstanceOf[this.type]
}

class MyQueue(
           dataBits: Int,
           strbBits: Int,
           depth: Int,
           pipe: Boolean = false,
           flow: Boolean = false
           ) extends BlackBox(
  Map(
    "p_DATA_BITS" -> IntParam(dataBits),
    "p_STRB_BITS" -> IntParam(strbBits),
    "p_FIFO_DEPTH" -> IntParam(depth),
    "p_PIPE" -> IntParam(if (pipe) 1 else 0),
    "p_FLOW" -> IntParam(if (flow) 1 else 0)
  )) with HasBlackBoxResource {

  val io = IO(new Bundle {
    val enq = Flipped(Decoupled(new MyData(dataBits)))
    val deq = Decoupled(new MyData(dataBits))
    val count = Output(UInt(log2Ceil(depth).W))
  })

  addResource("/MyQueue.sv")
}

class QueueWrapper(useVerilog: Boolean)(
                    dataBits: Int,
                    depth: Int,
                    pipe: Boolean = false,
                    flow: Boolean = false) extends Module {

  val strbBits = dataBits / 8

  val io = IO(new Bundle {
    val enq = Flipped(Decoupled(new MyData(dataBits)))
    val deq = Decoupled(new MyData(dataBits))
    val count = Output(UInt(log2Ceil(depth + 1).W))
  })

  if (useVerilog) {
    val m_queue = Module(new MyQueue(dataBits, strbBits, depth, pipe, flow))
    io <> m_queue.io
  } else {
    val m_queue = Module(new Queue(chiselTypeOf(io.enq.bits), depth, pipe, flow))
    io <> m_queue.io
  }
}

object Elaborate extends App {
  Driver.execute(Array("-td=rtl", "-tn=TrueQueue"),
    () => new QueueWrapper(true)(128, 1, false, false))
  Driver.execute(Array("-td=rtl", "-tn=FalseQueue"),
    () => new QueueWrapper(false)(128, 1, false, false))
}