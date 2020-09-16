

import chisel3._
import chisel3.util.{Cat, MuxCase, switch}

class ChiselAggregate extends Module {
  val io = IO(new Bundle {
    val init = Input(UInt(8.W))
    val vecOut = Output(Vec(10, UInt(8.W)))
  })

  //val wireVecErr = Vec(10, Wire(UInt(8.W)))

  val regInntVec = RegInit(VecInit(Seq(io.init) ++ Seq.fill(9)(0.U(8.W))))
  val regNextWoVec = RegNext(regInntVec)
  val regNextWtVec = RegNext(regInntVec, VecInit(Seq.fill(10)(0.U(8.W))))

  io.vecOut := regNextWoVec
}

object ElaborateChiselAggregate extends App {

  // RTL生成用
  chisel3.Driver.execute(Array(
    "--target-dir=rtl/chisel-basic"
  ),
    () => new ChiselAggregate
  )
}


/**
class ChiselDelayBuffer extends Module {
  val io = IO(new Bundle {
    val in = Input(Bool())
    val out = Output(Bool())
  })

  val regInitVec = RegInit(VecInit(Seq.fill(10)(false.B)))

  for (i <- 0 until 10) {
    if (i == 0) {
      regInitVec(i) := io.in
    } else {
      regInitVec(i) := regInitVec(i-1)
    }
  }

  io.out := regInitVec(9)
}
*/

/**
class RegFile extends Module {
  val io = IO(new Bundle {
    val rdaddr_0 = Input(UInt(5.W))
    val rddata_0 = Output(UInt(32.W))
    val rdaddr_1 = Input(UInt(5.W))
    val rddata_1 = Output(UInt(32.W))
    val wren = Input(Bool())
    val wraddr = Input(UInt(5.W))
    val wrdata = Input(UInt(32.W))
  })

  val regFileVec = RegInit(VecInit(Seq.fill(10)(false.B)))

  when (io.wren) {
    regFileVec(io.wraddr) := io.wrdata
  }

  io.rddata_0 := regFileVec(io.rdaddr_0)
  io.rddata_1 := regFileVec(io.rdaddr_1)
}
*/

/**
class RegFile extends Module {
  val io = IO(new Bundle {
    val rdaddr = Vec(2, Input(UInt(5.W)))
    val rddata = Vec(2, Output(UInt(32.W)))
    val wren = Input(Bool())
    val wraddr = Input(UInt(5.W))
    val wrdata = Input(UInt(32.W))
  })

  val regFileVec = RegInit(VecInit(Seq.fill(32)(false.B)))

  when (io.wren) {
    regFileVec(io.wraddr) := io.wrdata
  }

  for ((rddata, addr) <- io.rddata.zip(io.rdaddr)) {
    rddata := regFileVec(addr)
  }
}
*/

class RegFile extends Module {
  val io = IO(new Bundle {
    val rdaddr = Input(Vec(2, UInt(5.W)))
    val rddata = Vec(2, Output(UInt(32.W)))
    /*
    val rdaddr = Input(Vec(2, UInt(5.W)))
    val rddata = Output(Vec(2, UInt(32.W)))
     */
    val wren = Input(Bool())
    val wraddr = Input(UInt(5.W))
    val wrdata = Input(UInt(32.W))
  })

  require()

  val regFileVec = RegInit(VecInit(Seq.fill(32)(false.B)))

  val a = Cat(!8.U & 11.U)
  val b =  true.B && false.B
  when (io.wren) {
    regFileVec(io.wraddr) := io.wrdata
  }
  switch
  printf()

  a <> b
  MuxCase
  io.rddata.zip(io.rdaddr).foreach {
    case (data, addr) => data := regFileVec(addr)
  }
}

object ElaborateChiselDelayBuffer extends App {

  // RTL生成用
  chisel3.Driver.execute(Array(
    "--target-dir=rtl/chisel-basic"
  ),
    () => new RegFile
  )
}
