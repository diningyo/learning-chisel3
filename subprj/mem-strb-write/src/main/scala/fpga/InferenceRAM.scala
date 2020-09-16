
package fpga

import scala.language.reflectiveCalls
import chisel3._
import chisel3.util._


/**
  * Inference inference memory I/O
  * @param numOfMemWords Number of memory words.
  * @param dataBits Memory data width.
  */
class InferenceRAMIO(numOfMemWords: Int, dataBits: Int) extends Bundle {
  val clock = Input(Clock())
  val addr = Input(UInt(log2Ceil(numOfMemWords).W))
  val wren = Input(Bool())
  val wrstrb = Input(UInt((dataBits / 8).W))
  val wrdata = Input(UInt(dataBits.W))
  val rddata = Output(UInt(dataBits.W))

  override def cloneType: this.type = new InferenceRAMIO(numOfMemWords, dataBits).asInstanceOf[this.type]
}

/**
  * Memory module for Xilinx
  * @param numOfMemWords Number of memory words.
  * @param dataBits Memory data width.
  * @param numOfPorts Number of ports. It must be lower than equal to 2.
  */
class InferenceRAM(numOfMemWords: Int, dataBits: Int, numOfPorts: Int = 2) extends Module {

  val io = IO(new Bundle {
    val ports = Vec(numOfPorts, new InferenceRAMIO(numOfMemWords, dataBits))
  })

  require(numOfPorts <= 2, "Number of ports must be lower than equal to 2.")

  val mem = Mem(numOfMemWords, Vec(dataBits / 8, UInt(8.W)))

  for (port <- io.ports) {
    withClock (port.clock) {
      val vec_wrdata = VecInit(Range(0, dataBits, 8).map(i => port.wrdata(i + 7, i)))
      when (port.wren) {
        mem.write(port.addr, vec_wrdata, port.wrstrb.toBools)
      }
      port.rddata := Cat(RegNext(mem(port.addr)))
    }
  }
}

/**
  * Elaborate Xilinx memory module
  */
object Elaborate extends App {
  Driver.execute(Array(
    "-td=rtl/vec/strb-write-mem"
  ),
    () => new InferenceRAM(65536, 32, 2)
  )
}
