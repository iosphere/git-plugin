package hudson.plugins.git.extensions.impl;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.plugins.git.GitException;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.extensions.GitClientType;
import hudson.plugins.git.extensions.GitSCMExtension;
import hudson.plugins.git.extensions.GitSCMExtensionDescriptor;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;
import org.jenkinsci.plugins.gitclient.CloneCommand;
import org.jenkinsci.plugins.gitclient.FetchCommand;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public class CloneOption extends GitSCMExtension {

    private boolean shallow;
    private String reference;
    private boolean fetchFromReference;
    private Integer timeout;

    @DataBoundConstructor
    public CloneOption(boolean shallow, String reference, boolean fetchFromReference, Integer timeout) {
        this.shallow = shallow;
        this.reference = Util.fixEmptyAndTrim(reference);
        this.fetchFromReference = fetchFromReference;
        this.timeout = timeout;
    }

    public boolean isShallow() {
        return shallow;
    }

    public String getReference() {
        return reference;
    }
    
    public Integer getTimeout() {
        return timeout;
    }

    @Override
    public void decorateCloneCommand(GitSCM scm, AbstractBuild<?, ?> build, GitClient git, BuildListener listener,
                                     CloneCommand cmd) throws IOException, InterruptedException, GitException {
        if (shallow) {
            listener.getLogger().println("Using shallow clone");
            cmd.shallow();
        }
        cmd.timeout(timeout);
        cmd.reference(build.getEnvironment(listener).expand(reference));
    }
    
    @Override
    public void decorateFetchCommand(GitSCM scm, GitClient git, TaskListener listener, FetchCommand cmd, URIish uri,
                                     List<RefSpec> refSpecs) throws IOException,InterruptedException, GitException {
        // Apply custom timeout
        cmd.timeout(timeout);

        // Fetch from the local reference repository, rather than from the configured remote URI
        if (fetchFromReference) {
            // TODO: Expand variables somehow
            final String referencePath = reference;
            if (referencePath == null) {
                return;
            }

            // TODO: This will probably go badly if the path is not available on the machine, so check up front
            try {
                cmd.from(new URIish(String.format("file://%s", reference)), refSpecs);
                listener.getLogger().println(String.format("Performing fetch via local repository: %s", reference));
            } catch (URISyntaxException e) {
                throw new GitException(e);
            }
        }
    }

    @Override
    public GitClientType getRequiredClient() {
        return GitClientType.GITCLI;
    }

    @Extension
    public static class DescriptorImpl extends GitSCMExtensionDescriptor {
        @Override
        public String getDisplayName() {
            return "Advanced clone behaviours";
        }
    }

}
