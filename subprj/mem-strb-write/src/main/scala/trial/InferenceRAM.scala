
package trial

import scala.language.reflectiveCalls
import chisel3._
import chisel3.util._

/**
  *
  * @param numOfMemWords
  * @param dataBits
  * @param hasStrb
  * @param hasRden
  * @param hasPortB
  */
case class InferenceRAMParams
(
  numOfMemWords: Int,
  dataBits: Int,
  hasStrb: Boolean = false,
  hasRden: Boolean = false,
  hasPortB: Boolean = false
)

/**
  * Inference inference memory I/O
  * @param p RAM params.
  */
class InferenceRAMIO(p: InferenceRAMParams) extends Bundle {
  val clock = Input(Clock())
  val addr = Input(UInt(log2Ceil(p.numOfMemWords).W))
  val wren = Input(Bool())
  val wrstrb = if (p.hasStrb) Some(Input(UInt((p.dataBits / 8).W))) else None
  val wrdata = Input(UInt(p.dataBits.W))
  val rden = if (p.hasRden) Some(Input(Bool())) else None
  val rddata = Output(UInt(p.dataBits.W))

  override def cloneType: this.type = new InferenceRAMIO(p).asInstanceOf[this.type]
}

/**
  * Memory module for Xilinx
  * @param numOfMemWords Number of memory words.
  * @param dataBits Memory data width.
  * @param hasStrb If this parameter sets true, RAM would have write strobe port.
  * @param hasRden If this parameter sets true, RAM would have read enable port.
  * @param hasPortB If this parameter sets true, RAM would be dual port RAM.
  */
class InferenceRAM(
  numOfMemWords: Int,
  dataBits: Int,
  hasStrb: Boolean = true,
  hasRden: Boolean = false,
  hasPortB: Boolean = false) extends Module {

  val portCfg = InferenceRAMParams(
    numOfMemWords, dataBits, hasStrb, hasRden
  )

  val io = IO(new Bundle {
    val a = new InferenceRAMIO(portCfg)
    val b = if (hasPortB) Some(new InferenceRAMIO(portCfg)) else None
  })

  val mem = if (hasStrb) Mem(numOfMemWords, Vec(dataBits / 8, UInt(8.W)))
            else         Mem(numOfMemWords, UInt(dataBits.W))

  val ports = if (hasPortB) Seq(io.a, io.b.get) else Seq(io.a)

  for (port <- ports) {
    withClock (port.clock) {
      val wrdata = if (hasStrb) VecInit(Range(0, dataBits, 8).map(i => port.wrdata(i + 7, i)))
                   else         port.wrdata

      when (port.wren) {
        wrdata match {
          case d: Vec[_] =>
            mem.asInstanceOf[Mem[Vec[UInt]]].write(port.addr, d.asInstanceOf[Vec[UInt]], port.wrstrb.get.toBools)
          case d: UInt =>
            mem.asInstanceOf[Mem[UInt]].write(port.addr, d)
        }
      }
      val rddata = if (hasStrb) Cat(RegNext(mem.asInstanceOf[Mem[Vec[UInt]]].read(port.addr)))
                   else RegNext(mem.asInstanceOf[Mem[UInt]].read(port.addr))
      port.rddata := rddata
    }
  }
}

/**
  * Elaborate Xilinx memory module
  */
object Elaborate extends App {

  val numOfMemWords = 65536
  val dataBits = 32

  for (hasStrb <- Seq(false, true); hasRden <- Seq(false, true); hasPortB <- Seq(false, true)) {
    val s = if (hasStrb) "HasStrb" else "NoStrb"
    val r = if (hasRden) "HasRden" else "NoRden"
    val p = if (hasPortB) "HasPortB" else "NoPortB"
    Driver.execute(Array(
      "-td=rtl/trial",
      "-tn=RAM" + s + r + p
    ),
      () => new InferenceRAM(numOfMemWords, dataBits, hasStrb, hasRden, hasPortB)
    )
  }
}