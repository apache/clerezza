package org.apache.clerezza.sshshell;

import java.security.PrivilegedActionException;
import java.util.logging.Level;
import org.apache.clerezza.shell.Shell;
import org.apache.felix.scr.annotations.Reference;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivilegedExceptionAction;
import javax.security.auth.Subject;
import org.apache.felix.scr.annotations.Component;
import org.apache.clerezza.platform.security.auth.AuthenticationChecker;
import org.apache.clerezza.platform.security.UserUtil;
import org.apache.sshd.*;
import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.shell.ShellFactory;
import org.apache.felix.scr.annotations.Property;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 */
@Component(metatype = true, enabled = true)
public class SshShell {

	@Property(intValue = 8022, description = "The port on which the ssh service listens)")
	public static final String PORT = "port";
	@Reference
	private ShellFactory shellFactory;
	@Reference
	AuthenticationChecker authenticationChecker;
	public int port = 8022;
	private SshServer sshd;
	private static ThreadLocal<Subject> currentSubject = new ThreadLocal<Subject>();
	private static Logger log = LoggerFactory.getLogger(SshShell.class);

	public SshShell() {
		sshd = SshServer.setUpDefaultServer();
		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));
		sshd.setPasswordAuthenticator(new MyPasswordAuthenticator());
	}

	protected void activate(ComponentContext cc) throws IOException {
		port = (Integer) cc.getProperties().get(PORT);
		sshd.setPort(port);
		sshd.setShellFactory(new Factory<Command>() {

			@Override
			public Command create() {

				return new Command() {

					private InputStream in;
					private OutputStream out;
					private Shell shell;
					private ExitCallback ec;

					@Override
					public void setInputStream(InputStream in) {
						this.in = in;
					}

					@Override
					public void setOutputStream(OutputStream out) {
						this.out = out;
					}

					@Override
					public void setErrorStream(OutputStream out) {
					}

					@Override
					public void setExitCallback(ExitCallback ec) {
						this.ec = ec;
					}

					@Override
					public void start(Environment e) throws IOException {

						final OutputStream newLineWrapperStream = new OutputStream() {

							@Override
							public void write(int b) throws IOException {
								if (b == '\n') {
									out.write('\r');
									out.write('\n');
								} else {
									out.write(b);
								}
							}

							@Override
							public void flush() throws IOException {
								out.flush();
							}

							@Override
							public void close() throws IOException {
								out.close();
							}
						};
						Subject subject = currentSubject.get();
						log.debug("doing as {}", subject);
						try {
							Subject.doAsPrivileged(subject, new PrivilegedExceptionAction<Object>() {

								@Override
								public Object run() throws Exception {
									shell = shellFactory.createShell(in, newLineWrapperStream);
									shell.addTerminationListener(new Shell.TerminationListener() {

										public void terminated() {
											ec.onExit(0);
										}

									});
									shell.start();
									return null;
								}
							}, null);
						} catch (PrivilegedActionException ex) {
							Throwable cause = ex.getCause();
							if (cause instanceof RuntimeException) {
								throw (RuntimeException) cause;
							} else {
								throw new RuntimeException(cause);
							}
						}

					}

					@Override
					public void destroy() {
						if (shell != null) {
							shell.stop();
						}
						shell = null;
					}
				};
			}
		});

		sshd.start();
	}

	protected void deactivate(ComponentContext cc) throws Exception {
		sshd.stop();
	}

	private class MyPasswordAuthenticator implements PasswordAuthenticator {

		public MyPasswordAuthenticator() {
		}

		@Override
		public boolean authenticate(String userName, String password, ServerSession ss) {
			log.debug("Authenticating {}, {}.", userName, password);
			try {
				if (authenticationChecker.authenticate(userName, password)) {
					Subject subject = UserUtil.createSubject(userName);
					currentSubject.set(subject);
					return true;
				} else {
					return false;
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
