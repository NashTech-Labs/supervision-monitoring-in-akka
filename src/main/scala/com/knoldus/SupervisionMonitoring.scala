package com.knoldus

import akka.actor.SupervisorStrategy._
import akka.actor._
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class Child extends Actor {

  val log = Logging(context.system, this)

  import Exceptions._

  override def preStart() = {
    val actorName = self.path
    log.info(s"Child preStart with path: $actorName")
  }

  override def preRestart(reason: Throwable, message: Option[Any]) = {
    log.info(s"Child preRestart - $message - $reason")
  }

  override def postStop() = {
    log.info("Child Stop")
  }

  def receive = {
    case "Stop" =>
      throw StopException
    case "Restart" =>
      throw RestartException
    case "Resume" =>
      throw ResumeException
    case _ =>
      throw new Exception
  }

}

class Parent extends Actor {

  import Exceptions._

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 second) {
      case ResumeException => Resume
      case RestartException => Restart
      case StopException => Stop
      case _: Exception => Escalate
    }
  val log = Logging(context.system, this)

  override def preStart() = {
    log.info(s"Parent preStart with path: ${self.path}")
  }

  override def preRestart(reason: Throwable, message: Option[Any]) = {
    log.info(s"Parent preRestart - $message - $reason")
  }

  override def postStop() = {
    log.info("Parent Stop")
  }

  def receive = {
    case Terminated(actor) =>
      log.warning(s"Child Actor died: $actor")
      context.unwatch(actor)
      self ! PoisonPill
    case (prop: Props, name: String) =>
      val child = context.actorOf(prop, name)
      sender ! child
      context.watch(child)
  }

}

object Exceptions {

  case object ResumeException extends Exception

  case object StopException extends Exception

  case object RestartException extends Exception

}

object SupervisionMonitoring extends App {
  implicit val timeout = Timeout(10 seconds)
  val system = ActorSystem("supervision-monitoring")
  val parent = system.actorOf(Props[Parent], "parent")
  system.eventStream.subscribe(parent, classOf[DeadLetter])
  val child = (parent ? (Props[Child], "child")).mapTo[ActorRef]
  child onComplete {
    case Success(childRef) =>
      childRef ! "Stop"
    case Failure(t) => println("An error has occurred: " + t.getMessage)
  }
  Thread.sleep(1000)
  system.terminate()
}
