package hudson.plugins.git.extensions.impl;

import hudson.Extension;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.plugins.git.GitException;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.extensions.GitClientType;
import hudson.plugins.git.extensions.GitSCMExtension;
import hudson.plugins.git.extensions.GitSCMExtensionDescriptor;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;
import org.jenkinsci.plugins.gitclient.FetchCommand;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;

public class FetchOption extends GitSCMExtension {

    private String localFetchUri;

    @DataBoundConstructor
    public FetchOption(String localFetchUri) {
        this.localFetchUri = Util.fixEmptyAndTrim(localFetchUri);
    }

    public String getLocalFetchUri() {
        return localFetchUri;
    }

    @Override
    public void decorateFetchCommand(GitSCM scm, GitClient git, TaskListener listener, FetchCommand cmd) throws IOException, InterruptedException, GitException {
        if (localFetchUri != null) {
            try {
                URIish uri = new URIish("file://" + localFetchUri);
                // TODO: FetchCommand should have getters, or GitSCM should pass in the RemoteConfig and URI
                // TODO: Otherwise we don't know what the refspec is for this particular fetch operation
                cmd.from(uri, Collections.singletonList(new RefSpec("+refs/heads/*:refs/remotes/origin/*")));
                listener.getLogger().println("Rewriting fetch to use local repository: " + uri);
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
            return "Advanced fetch behaviours";
        }
    }

}
