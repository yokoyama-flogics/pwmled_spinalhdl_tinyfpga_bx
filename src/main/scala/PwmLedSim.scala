// package com.flogics.spinal.pwmled

import spinal.core._
import spinal.sim._
import spinal.core.sim._

object PwmLedSim {
  def main(args: Array[String]): Unit = {
    SimConfig.withWave.compile(new PwmLed(divSize = 3, pwmSize = 4)).
        doSim{ dut =>
      dut.clockDomain.forkStimulus(period = 10)
      for (i <- 0 until 500) {
        dut.clockDomain.waitSampling()
      }
    }
  }
}
