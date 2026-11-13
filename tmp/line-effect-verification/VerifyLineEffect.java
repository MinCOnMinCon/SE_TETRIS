import org.junit.platform.launcher.core.*;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import static org.junit.platform.engine.discovery.DiscoverySelectors.*;
public class VerifyLineEffect {
  public static void main(String[] args) {
    var request = LauncherDiscoveryRequestBuilder.request().selectors(
      selectPackage("tetris.block.move"), selectClass("tetris.block.blocks.BlockDeleteTest"),
      selectClass("tetris.gamescene.GameControllerTest"), selectClass("tetris.gamescene.PlayerInputTest")
    ).build();
    var listener = new SummaryGeneratingListener();
    LauncherFactory.create().execute(request, listener);
    var writer = new java.io.PrintWriter(System.out);
    listener.getSummary().printTo(writer);
    listener.getSummary().printFailuresTo(writer);
    System.exit(listener.getSummary().getTestsFailedCount() == 0 ? 0 : 1);
  }
}
