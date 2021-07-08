package org.apache.clerezza.ontologies;

import org.apache.clerezza.IRI;

public class FOAF {
	// Classes

	/**
	 * comment: An agent (eg. person, group, software or physical artifact).

	 */
	public static final IRI Agent = new IRI("http://xmlns.com/foaf/0.1/Agent");

	/**
	 * comment: A document.

	 */
	public static final IRI Document = new IRI("http://xmlns.com/foaf/0.1/Document");

	/**
	 * comment: A class of Agents.

	 */
	public static final IRI Group = new IRI("http://xmlns.com/foaf/0.1/Group");

	/**
	 * comment: An image.

	 */
	public static final IRI Image = new IRI("http://xmlns.com/foaf/0.1/Image");

	/**
	 * comment: A foaf:LabelProperty is any RDF property with texual values that serve as labels.

	 */
	public static final IRI LabelProperty = new IRI("http://xmlns.com/foaf/0.1/LabelProperty");

	/**
	 * comment: An online account.

	 */
	public static final IRI OnlineAccount = new IRI("http://xmlns.com/foaf/0.1/OnlineAccount");

	/**
	 * comment: An online chat account.

	 */
	public static final IRI OnlineChatAccount = new IRI("http://xmlns.com/foaf/0.1/OnlineChatAccount");

	/**
	 * comment: An online e-commerce account.

	 */
	public static final IRI OnlineEcommerceAccount = new IRI("http://xmlns.com/foaf/0.1/OnlineEcommerceAccount");

	/**
	 * comment: An online gaming account.

	 */
	public static final IRI OnlineGamingAccount = new IRI("http://xmlns.com/foaf/0.1/OnlineGamingAccount");

	/**
	 * comment: An organization.

	 */
	public static final IRI Organization = new IRI("http://xmlns.com/foaf/0.1/Organization");

	/**
	 * comment: A person.

	 */
	public static final IRI Person = new IRI("http://xmlns.com/foaf/0.1/Person");

	/**
	 * comment: A personal profile RDF document.

	 */
	public static final IRI PersonalProfileDocument = new IRI("http://xmlns.com/foaf/0.1/PersonalProfileDocument");

	/**
	 * comment: A project (a collective endeavour of some kind).

	 */
	public static final IRI Project = new IRI("http://xmlns.com/foaf/0.1/Project");

	// Properties

	/**
	 * comment: Indicates an account held by this agent.

	 */
	public static final IRI account = new IRI("http://xmlns.com/foaf/0.1/account");

	/**
	 * comment: Indicates the name (identifier) associated with this online account.

	 */
	public static final IRI accountName = new IRI("http://xmlns.com/foaf/0.1/accountName");

	/**
	 * comment: Indicates a homepage of the service provide for this online account.

	 */
	public static final IRI accountServiceHomepage = new IRI("http://xmlns.com/foaf/0.1/accountServiceHomepage");

	/**
	 * comment: The age in years of some agent.

	 */
	public static final IRI age = new IRI("http://xmlns.com/foaf/0.1/age");

	/**
	 * comment: An AIM chat ID

	 */
	public static final IRI aimChatID = new IRI("http://xmlns.com/foaf/0.1/aimChatID");

	/**
	 * comment: A location that something is based near, for some broadly human notion of near.

	 */
	public static final IRI based_near = new IRI("http://xmlns.com/foaf/0.1/based_near");

	/**
	 * comment: The birthday of this Agent, represented in mm-dd string form, eg. '12-31'.

	 */
	public static final IRI birthday = new IRI("http://xmlns.com/foaf/0.1/birthday");

	/**
	 * comment: A current project this person works on.

	 */
	public static final IRI currentProject = new IRI("http://xmlns.com/foaf/0.1/currentProject");

	/**
	 * comment: A depiction of some thing.

	 */
	public static final IRI depiction = new IRI("http://xmlns.com/foaf/0.1/depiction");

	/**
	 * comment: A thing depicted in this representation.

	 */
	public static final IRI depicts = new IRI("http://xmlns.com/foaf/0.1/depicts");

	/**
	 * comment: A checksum for the DNA of some thing. Joke.

	 */
	public static final IRI dnaChecksum = new IRI("http://xmlns.com/foaf/0.1/dnaChecksum");

	/**
	 * comment: The family name of some person.

	 */
	public static final IRI familyName = new IRI("http://xmlns.com/foaf/0.1/familyName");

	/**
	 * comment: The family name of some person.

	 */
	public static final IRI family_name = new IRI("http://xmlns.com/foaf/0.1/family_name");

	/**
	 * comment: The first name of a person.

	 */
	public static final IRI firstName = new IRI("http://xmlns.com/foaf/0.1/firstName");

	/**
	 * comment: An organization funding a project or person.

	 */
	public static final IRI fundedBy = new IRI("http://xmlns.com/foaf/0.1/fundedBy");

	/**
	 * comment: A textual geekcode for this person, see http://www.geekcode.com/geek.html

	 */
	public static final IRI geekcode = new IRI("http://xmlns.com/foaf/0.1/geekcode");

	/**
	 * comment: The gender of this Agent (typically but not necessarily 'male' or 'female').

	 */
	public static final IRI gender = new IRI("http://xmlns.com/foaf/0.1/gender");

	/**
	 * comment: The given name of some person.

	 */
	public static final IRI givenName = new IRI("http://xmlns.com/foaf/0.1/givenName");

	/**
	 * comment: The given name of some person.

	 */
	public static final IRI givenname = new IRI("http://xmlns.com/foaf/0.1/givenname");

	/**
	 * comment: Indicates an account held by this agent.

	 */
	public static final IRI holdsAccount = new IRI("http://xmlns.com/foaf/0.1/holdsAccount");

	/**
	 * comment: A homepage for some thing.

	 */
	public static final IRI homepage = new IRI("http://xmlns.com/foaf/0.1/homepage");

	/**
	 * comment: An ICQ chat ID

	 */
	public static final IRI icqChatID = new IRI("http://xmlns.com/foaf/0.1/icqChatID");

	/**
	 * comment: An image that can be used to represent some thing (ie. those depictions which are particularly representative of something, eg. one's photo on a homepage).

	 */
	public static final IRI img = new IRI("http://xmlns.com/foaf/0.1/img");

	/**
	 * comment: A page about a topic of interest to this person.

	 */
	public static final IRI interest = new IRI("http://xmlns.com/foaf/0.1/interest");

	/**
	 * comment: A document that this thing is the primary topic of.

	 */
	public static final IRI isPrimaryTopicOf = new IRI("http://xmlns.com/foaf/0.1/isPrimaryTopicOf");

	/**
	 * comment: A jabber ID for something.

	 */
	public static final IRI jabberID = new IRI("http://xmlns.com/foaf/0.1/jabberID");

	/**
	 * comment: A person known by this person (indicating some level of reciprocated interaction between the parties).

	 */
	public static final IRI knows = new IRI("http://xmlns.com/foaf/0.1/knows");

	/**
	 * comment: The last name of a person.

	 */
	public static final IRI lastName = new IRI("http://xmlns.com/foaf/0.1/lastName");

	/**
	 * comment: A logo representing some thing.

	 */
	public static final IRI logo = new IRI("http://xmlns.com/foaf/0.1/logo");

	/**
	 * comment: Something that was made by this agent.

	 */
	public static final IRI made = new IRI("http://xmlns.com/foaf/0.1/made");

	/**
	 * comment: An agent that  made this thing.

	 */
	public static final IRI maker = new IRI("http://xmlns.com/foaf/0.1/maker");

	/**
	 * comment: A  personal mailbox, ie. an Internet mailbox associated with exactly one owner, the first owner of this mailbox. This is a 'static inverse functional property', in that  there is (across time and change) at most one individual that ever has any particular value for foaf:mbox.

	 */
	public static final IRI mbox = new IRI("http://xmlns.com/foaf/0.1/mbox");

	/**
	 * comment: The sha1sum of the URI of an Internet mailbox associated with exactly one owner, the  first owner of the mailbox.

	 */
	public static final IRI mbox_sha1sum = new IRI("http://xmlns.com/foaf/0.1/mbox_sha1sum");

	/**
	 * comment: Indicates a member of a Group

	 */
	public static final IRI member = new IRI("http://xmlns.com/foaf/0.1/member");

	/**
	 * comment: Indicates the class of individuals that are a member of a Group

	 */
	public static final IRI membershipClass = new IRI("http://xmlns.com/foaf/0.1/membershipClass");

	/**
	 * comment: An MSN chat ID

	 */
	public static final IRI msnChatID = new IRI("http://xmlns.com/foaf/0.1/msnChatID");

	/**
	 * comment: A Myers Briggs (MBTI) personality classification.

	 */
	public static final IRI myersBriggs = new IRI("http://xmlns.com/foaf/0.1/myersBriggs");

	/**
	 * comment: A name for some thing.

	 */
	public static final IRI name = new IRI("http://xmlns.com/foaf/0.1/name");

	/**
	 * comment: A short informal nickname characterising an agent (includes login identifiers, IRC and other chat nicknames).

	 */
	public static final IRI nick = new IRI("http://xmlns.com/foaf/0.1/nick");

	/**
	 * comment: An OpenID for an Agent.

	 */
	public static final IRI openid = new IRI("http://xmlns.com/foaf/0.1/openid");

	/**
	 * comment: A page or document about this thing.

	 */
	public static final IRI page = new IRI("http://xmlns.com/foaf/0.1/page");

	/**
	 * comment: A project this person has previously worked on.

	 */
	public static final IRI pastProject = new IRI("http://xmlns.com/foaf/0.1/pastProject");

	/**
	 * comment: A phone,  specified using fully qualified tel: URI scheme (refs: http://www.w3.org/Addressing/schemes.html#tel).

	 */
	public static final IRI phone = new IRI("http://xmlns.com/foaf/0.1/phone");

	/**
	 * comment: A .plan comment, in the tradition of finger and '.plan' files.

	 */
	public static final IRI plan = new IRI("http://xmlns.com/foaf/0.1/plan");

	/**
	 * comment: The primary topic of some page or document.

	 */
	public static final IRI primaryTopic = new IRI("http://xmlns.com/foaf/0.1/primaryTopic");

	/**
	 * comment: A link to the publications of this person.

	 */
	public static final IRI publications = new IRI("http://xmlns.com/foaf/0.1/publications");

	/**
	 * comment: A homepage of a school attended by the person.

	 */
	public static final IRI schoolHomepage = new IRI("http://xmlns.com/foaf/0.1/schoolHomepage");

	/**
	 * comment: A sha1sum hash, in hex.

	 */
	public static final IRI sha1 = new IRI("http://xmlns.com/foaf/0.1/sha1");

	/**
	 * comment: A Skype ID

	 */
	public static final IRI skypeID = new IRI("http://xmlns.com/foaf/0.1/skypeID");

	/**
	 * comment: A string expressing what the user is happy for the general public (normally) to know about their current activity.

	 */
	public static final IRI status = new IRI("http://xmlns.com/foaf/0.1/status");

	/**
	 * comment: The surname of some person.

	 */
	public static final IRI surname = new IRI("http://xmlns.com/foaf/0.1/surname");

	/**
	 * comment: A theme.

	 */
	public static final IRI theme = new IRI("http://xmlns.com/foaf/0.1/theme");

	/**
	 * comment: A derived thumbnail image.

	 */
	public static final IRI thumbnail = new IRI("http://xmlns.com/foaf/0.1/thumbnail");

	/**
	 * comment: A tipjar document for this agent, describing means for payment and reward.

	 */
	public static final IRI tipjar = new IRI("http://xmlns.com/foaf/0.1/tipjar");

	/**
	 * comment: Title (Mr, Mrs, Ms, Dr. etc)

	 */
	public static final IRI title = new IRI("http://xmlns.com/foaf/0.1/title");

	/**
	 * comment: A topic of some page or document.

	 */
	public static final IRI topic = new IRI("http://xmlns.com/foaf/0.1/topic");

	/**
	 * comment: A thing of interest to this person.

	 */
	public static final IRI topic_interest = new IRI("http://xmlns.com/foaf/0.1/topic_interest");

	/**
	 * comment: A weblog of some thing (whether person, group, company etc.).

	 */
	public static final IRI weblog = new IRI("http://xmlns.com/foaf/0.1/weblog");

	/**
	 * comment: A work info homepage of some person; a page about their work for some organization.

	 */
	public static final IRI workInfoHomepage = new IRI("http://xmlns.com/foaf/0.1/workInfoHomepage");

	/**
	 * comment: A workplace homepage of some person; the homepage of an organization they work for.

	 */
	public static final IRI workplaceHomepage = new IRI("http://xmlns.com/foaf/0.1/workplaceHomepage");

	/**
	 * comment: A Yahoo chat ID

	 */
	public static final IRI yahooChatID = new IRI("http://xmlns.com/foaf/0.1/yahooChatID");

	// Properties

	/**
	 * 
	 */
	public static final IRI THIS_ONTOLOGY = new IRI("http://xmlns.com/foaf/0.1/");
}
