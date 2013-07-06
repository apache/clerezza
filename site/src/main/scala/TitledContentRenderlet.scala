package org.apache.clerezza.site

import javax.ws.rs.core.MediaType
import org.apache.clerezza.platform.typerendering._
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.rdf.utils.GraphNode
import org.apache.clerezza.rdf.ontologies._
import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.utils._
import org.apache.clerezza.rdf.scala.utils.Preamble._
import org.apache.clerezza.platform.typerendering.scala._
import org.apache.clerezza.rdf.ontologies.DC

/**
 * A Renderlet for the menu
 */
class TitledContentRenderlet extends SRenderlet {

	val getRdfType = DISCOBITS.TitledContent

	override def getModePattern = null

	override def renderedPage(arguments: XmlResult.Arguments) = {
		new XmlResult(arguments) {
			def menuLink(href: String, label: String) =
			if ((res*).endsWith(href) || (res*).endsWith(href+"index")) {
				 <a href={href} class="active">{label}</a>
			} else {
				 <a href={href}>{label}</a>
			}
			override def content = {
				<html xmlns="http://www.w3.org/1999/xhtml">
					<head>
						<link type="text/css" href="/style/style.css" rel="stylesheet" />
						{(res/DISCOBITS.contains).find(e => ((e/DISCOBITS.pos*) == "0")) match {
								case Some(e) => <title>{render(e/DISCOBITS.holds, "naked")}</title>
								case None => <title>An incomplete titled content {res/DISCOBITS.contains*}</title>
							}
						}
					</head>
					<body>
						<div class="zz-header">

							<div class="bar"></div>
							<div class="logo">
								<a href="http://clerezza.apache.org/" style=""><img src="/images/logo.png" alt="logo" /></a>
							</div>
						</div>
						<div class="column nav">
							<ul>
								<li class="top-nav-entry"><div class="title">Documentation</div>

									<ul class="nav-entries">
										<li>{menuLink("/getting-started/","Getting Started")}</li>
										<li>{menuLink("/architecture/","The Apache Clerezza Stack")}</li>
										<li><a href="http://clerezza.apache.org/apidoc" target="_blank">API docs</a></li>
										<li>{menuLink("/faq/","FAQ")}</li>
									</ul>
								</li>

								<li class="top-nav-entry"><div class="title">Project Infos</div>
									<ul  class="nav-entries">
										<li>{menuLink("/downloads/","Downloads")}</li>
										<li>{menuLink("/contributing/", "Contributing")}</li>
										<li><a href="http://www.apache.org/licenses/" target="_blank">License</a></li>
										<li>{menuLink("/mailinglists/","Mailing lists")}</li>
										<li><a href="http://issues.apache.org/jira/browse/CLEREZZA" target="_blank">Issue Tracker</a></li>

										<li><a href="http://svn.apache.org/viewvc/clerezza/trunk/" target="_blank">Source Repository</a></li>
									</ul>
								</li>
								<li class="top-nav-entry"><div class="title">Sponsorship</div>
									<ul  class="nav-entries">
										<li><a href="/thanks/">Thanks</a></li>
										<li><a href="http://www.apache.org/foundation/sponsorship.html" target="_blank">Become a Sponsor</a></li>
										<li><a href="http://www.apache.org/foundation/buy_stuff.html" target="_blank">Buy Stuff</a></li>
									</ul>
								</li>
							</ul>
						</div>

						<div class="zz-content">
							{render(res, "naked")}
						</div>
						<div class="footer">
							<div class="logos"><img src="/images/feather.png" /><img src="/images/sw-vert-w3c.png" /><img src="/images/footer-logo.png" /></div>

							<div class="divider"></div>
							<div class="dark">
								<div class="sitemap">
									<div class="sitemap-title">Sitemap</div>
									<div class="sitemap-content">
										<div class="sitemap-column">
											<div class="title" >Documentation</div>
											<ul>

												<li><a href="/getting-started/">Getting Started</a></li>
												<li><a href="/architecture/">The Apache Clerezza Stack</a></li>
												<li><a href="http://clerezza,apache.org/apidoc" target="_blank">API docs</a></li>
												<li><a href="/faq/">FAQ</a></li>
											</ul>

										</div>
										<div class="sitemap-column">

											<div class="title" >Project Infos</div>
											<ul >
												<li><a href="/downloads/">Downloads</a></li>
												<li><a href="/contributing/">Contributing</a></li>
												<li><a href="http://www.apache.org/licenses/" target="_blank">License</a></li>
												<li><a href="mailinglists/">Mailing lists</a></li>
												<li><a href="http://issues.apache.org/jira/browse/CLEREZZA" target="_blank">Issue Tracker</a></li>

												<li><a href="http://svn.apache.org/viewvc/clerezza/trunk/" target="_blank">Source Repository</a></li>
											</ul>
										</div>
										<div class="sitemap-column">
											<div class="title" >Sponsorship</div>
											<ul>
												<li><a href="/thanks/">Thanks</a></li>
												<li><a href="http://www.apache.org/foundation/sponsorship.html" target="_blank">Become a Sponsor</a></li>
												<li><a href="http://www.apache.org/foundation/buy_stuff.html" target="_blank">Buy Stuff</a></li>
											</ul>
										</div>
									</div>

								</div>
								<div class="copyright">Apache Clerezza, Clerezza, Apache, the Apache feather logo, and the Apache Clerezza project logo are trademarks of The Apache Software Foundation. <br></br>© 2011 The Apache Software Foundation.</div>
							</div>
						</div>


					</body>
				</html>
			}
		}
	}

}
