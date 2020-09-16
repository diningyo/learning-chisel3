// See README.md for license details.

import java.awt.image.SampleModel

import Params.name
import chisel3._
import chisel3.util._
import firrtl.AnnotationSeq

class SampleModule extends Module {
  val io = IO(new Bundle {
    val in = Input(Bool())
    val out = Output(Bool())
  })
  io.out := io.in
}

object Params {
  val name = "SampleModule"
}

object OldElaborate extends App {
  import Params._

  Driver.execute(Array(
    "-td=rtl",
    s"-tn=$name"
  ),
    () => new SampleModule
  )
}

object NewElaborate extends App {
  val name = "SampleModule"

  (new stage.ChiselStage).execute(
    Array("-td=rtl", s"-o=$name"),
    Seq(chisel3.stage.ChiselGeneratorAnnotation(
      () => new SampleModule)))

  val rtl = (new stage.ChiselStage).emitVerilog(
    new SampleModule,
    Array("-td=rtl", s"-o=$name"))

  print(rtl)
}
