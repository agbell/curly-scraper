package com.cascadeofinsights.scraper.devUtils

import java.nio.file.Paths

import com.cascadeofinsights.scraper.models._
import com.cascadeofinsights.scraper.Scraper
import scalaz.Scalaz._
import scalaz.zio.console._
import scalaz.zio.{App, IO}


object ScalazCrawl extends App {

  val rootFilePath = Paths.get("/Users/adam/data")
  val start = Set(
    URL("https://scalaz.github.io/7/").get
  )

  import replRTS._

  val scraper: IO[Nothing, Crawl[Unit, List[(URL, String)]]] = Scraper.crawlIOPar(
    start,
    Routers.compose(
      Routers.stayInSeedDomainRouter(start),
      Routers.dropAnchorsAndQueryParams,
    ),
    Processors.returnAndCache(rootFilePath),
    Gets.getURLCached(rootFilePath)
  )

  def run(args: List[String]): IO[Nothing, ExitStatus] =
    (for {
      _ <- putStrLn("Starting")
      rs <- scraper
      print = rs.value.mkString("\n")
      _ <- putStrLn(s"results : \n$print")
    } yield
      ()).redeemPure(
      _ => ExitStatus.ExitNow(1),
      _ => ExitStatus.ExitNow(0)
    )


  val results: Crawl[Unit, List[(URL, String)]] = scraper.unsafeRun
  val firstPage: String = scraper.unsafeRun.value.head._2
  val urls: List[URL] = URL.extractURLs(URL("https://scalaz.github.io/7/").get,ScalazCrawl.firstPage)
  val urlsCleaned: List[URL] = urls.flatMap(u => Routers.stayInSeedDomainRouter(Set(start.head))(u))
}
