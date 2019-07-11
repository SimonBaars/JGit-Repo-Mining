package nl.simonbaars.bugcommit;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

public class VCSMine {

	private static String someCommit = "610694446a2eff51b0550689d7da5988bb6e2f2a";
	
	public static void main(String[] args) throws IOException {
		FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
		repositoryBuilder.setMustExist( true );
		repositoryBuilder.findGitDir(new File("/home/simon/Downloads/HACKATHON/elasticsearch"));
		Repository repository = repositoryBuilder.build();

		ObjectId commitId = ObjectId.fromString(someCommit);
		try (RevWalk revWalk = new RevWalk(repository)) {
		  RevCommit commit = revWalk.parseCommit(commitId);
		  
		  System.out.println(commit.getTree());
		  
		  try (TreeWalk treeWalk = new TreeWalk(repository)) {
			  treeWalk.reset(commit.getTree());
			  while (treeWalk.next()) {
			    String path = treeWalk.getPathString();
			    System.out.println(path);
			  }
			}
		}

	}
	
	public void balh(Repository repository) throws IOException, GitAPIException {
		// the diff works on TreeIterators, we prepare two for the two branches
        AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, "b97b184b0ce11c0b6a4dcc2b57768ff155cb696b");
        AbstractTreeIterator newTreeParser = prepareTreeParser(repository, "9e0719d7d773b41b49ebf04e6fd7b5c637e96063");

        // then the porcelain diff-command returns a list of diff entries
        try (Git git = new Git(repository)) {
            List<DiffEntry> diff = git.diff().
                    setOldTree(oldTreeParser).
                    setNewTree(newTreeParser).
                    setPathFilter(PathFilter.create("README.md")).
                    // to filter on Suffix use the following instead
                    //setPathFilter(PathSuffixFilter.create(".java")).
                    call();
            for (DiffEntry entry : diff) {
                System.out.println("Entry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId());
                try (DiffFormatter formatter = new DiffFormatter(System.out)) {
                    formatter.setRepository(repository);
                    formatter.format(entry);
                }
            }
        }
	}
	
	private static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
        // from the commit we can build the tree which allows us to construct the TreeParser
        //noinspection Duplicates
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(ObjectId.fromString(objectId));
            RevTree tree = walk.parseTree(commit.getTree().getId());

            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }

            walk.dispose();

            return treeParser;
        }
    }
	
	private static void branches(Repository repo) throws GitAPIException, MissingObjectException, IncorrectObjectTypeException, IOException {
	    Git git = new Git(repo);
	    RevWalk walk = new RevWalk(repo);

	    List<Ref> branches = git.branchList().call();

	    for (Ref branch : branches) {
	        String branchName = branch.getName();

	        System.out.println("Commits of branch: " + branch.getName());
	        System.out.println("-------------------------------------");

	        Iterable<RevCommit> commits = git.log().all().call();

	        for (RevCommit commit : commits) {
	            boolean foundInThisBranch = false;

	            RevCommit targetCommit = walk.parseCommit(repo.resolve(
	                    commit.getName()));
	            for (Map.Entry<String, Ref> e : repo.getAllRefs().entrySet()) {
	                if (e.getKey().startsWith(Constants.R_HEADS)) {
	                    if (walk.isMergedInto(targetCommit, walk.parseCommit(
	                            e.getValue().getObjectId()))) {
	                        String foundInBranch = e.getValue().getName();
	                        if (branchName.equals(foundInBranch)) {
	                            foundInThisBranch = true;
	                            break;
	                        }
	                    }
	                }
	            }

	            if (foundInThisBranch) {
	                System.out.println(commit.getName());
	                System.out.println(commit.getAuthorIdent().getName());
	                System.out.println(new Date(commit.getCommitTime() * 1000L));
	                System.out.println(commit.getFullMessage());
	            }
	        }
	    }
	}
}
