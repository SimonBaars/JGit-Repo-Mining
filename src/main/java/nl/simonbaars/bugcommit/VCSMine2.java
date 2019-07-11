package nl.simonbaars.bugcommit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.PathFilter;

public class VCSMine2 {

	private static String someCommit = "610694446a2eff51b0550689d7da5988bb6e2f2a";
	
	public static void main(String[] args) throws IOException, GitAPIException {
		FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
		repositoryBuilder.setMustExist( true );
		repositoryBuilder.findGitDir(new File("/home/simon/Downloads/HACKATHON/elasticsearch"));
		Repository repository = repositoryBuilder.build();

		branches(repository);

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



	        Iterable<RevCommit> commits = git.log().all().call();
	        List<RevCommit> comm = makeCollection(commits);

	        for (int i = 0; i<comm.size(); i++) {
	        	//System.out.println(comm.get(i).getId().isId("610694446a2eff51b0550689d7da5988bb6e2f2a"));
	            if(comm.get(i).getId().isId("610694446a2eff51b0550689d7da5988bb6e2f2a")) {
	            	System.out.println("Found "+i+", "+comm.get(i).getCommitTime());
	            }
	        }
	}

	public static <E> List<E> makeCollection(Iterable<E> iter) {
	    List<E> list = new ArrayList<>();
	    for (E item : iter) {
	        list.add(item);
	    }
	    return list;
	}
}
