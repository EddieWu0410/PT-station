package api;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api")
public interface ApiInterface {

    @PostMapping("/save-torrent")
    ResponseEntity<Integer> saveTorrent(
        @RequestParam("userid") String userid,
        @RequestParam("title") String title,
        @RequestParam("tag") String tag,
        @RequestParam("file") MultipartFile file
    );

    @GetMapping("/get-torrent")
    ResponseEntity<Resource> getTorrent(
        @RequestParam("torrentId") String seedid,
        @RequestParam("userId") String userid
    );

    @GetMapping("/get-seed-list-by-tag")
    ResponseEntity<String> getSeedListByTag(
        @RequestParam("tag") String tag
    );

    @GetMapping("/torrent-detail")
    ResponseEntity<String> getTorrentDetail(
        @RequestParam("id") String seedid
    );

    @GetMapping("user-profile")
    ResponseEntity<String> getUserProfile(
        @RequestParam("id") String userid
    );

    @PostMapping("/change-profile")
    ResponseEntity<Integer> changeProfile(
        @RequestBody String requestBody
    );

    @GetMapping("/user-seeds")
    ResponseEntity<String> getUserSeeds(
        @RequestParam("userid") String userid
    );

    @PostMapping("/delete-seed")
    ResponseEntity<Integer> deleteSeed(
        @RequestBody String requestBody
    );

    @GetMapping("/user-stat")
    ResponseEntity<String> getUserStat(
        @RequestParam("userid") String userid
    );

    @PostMapping("/login")
    ResponseEntity<String> loginUser(
        @RequestBody String requestBody
    );

    @PostMapping("/register")
    ResponseEntity<Integer> registerUser(
        @RequestBody String requestBody
    );

    @GetMapping("/forum")
    ResponseEntity<String> getForum();

    @GetMapping("/forum-detail")
    ResponseEntity<String> getPostById(
        @RequestParam("postid") String postid
    );

    @PostMapping("/forum-reply")
    ResponseEntity<Integer> addPostReply(
        @RequestBody String requestBody
    );

    @GetMapping("/search-seeds")
    ResponseEntity<String> searchSeeds(
        @RequestParam("tag") String tag,
        @RequestParam("keyword") String query
    );

    @GetMapping("/search-posts")
    ResponseEntity<String> searchPosts(
        @RequestParam("keyword") String query
    );

    @GetMapping("/get-userpt")
    ResponseEntity<String> getUserPT(
        @RequestParam("userid") String userid
    );

    @GetMapping("/admin/config")
    ResponseEntity<String> getConfig(
        @RequestParam("userid") String userid
    );

    @GetMapping("/admin/cheat-users")
    ResponseEntity<String> getCheatUsers(
        @RequestParam("userid") String userid
    );

    @GetMapping("/admin/suspicious-users")
    ResponseEntity<String> getSuspiciousUsers(
        @RequestParam("userid") String userid
    );

    @PostMapping("/admin/unban-user")
    ResponseEntity<Integer> unbanUser(
        @RequestBody String requestBody
    );

    @PostMapping("/admin/ban-user")
    ResponseEntity<Integer> banUser(
        @RequestBody String requestBody
    );

    @GetMapping("/appeals")
    ResponseEntity<String> getAppeals();

    @GetMapping("/migrations")
    ResponseEntity<String> getMigrations();

    @PostMapping("/appeals-approve")
    ResponseEntity<Integer> approveAppeal(
        @RequestBody String requestBody
    );

    @PostMapping("/appeals-reject")
    ResponseEntity<Integer> rejectAppeal(
        @RequestBody String requestBody
    );

    @PostMapping("/migrations-approve")
    ResponseEntity<Integer> approveMigration(
        @RequestBody String requestBody
    );

    @PostMapping("/migrations-reject")
    ResponseEntity<Integer> rejectMigration(
        @RequestBody String requestBody
    );

    @PostMapping("/invite")
    ResponseEntity<Integer> inviteUser(
        @RequestBody String requestBody
    );

    @PostMapping("/submit-appeal")
    ResponseEntity<Integer> submitAppeal(
        @RequestParam("userid") String userid,
        @RequestParam("content") String content,
        @RequestParam("file") MultipartFile file
    );
    
    @GetMapping("/user-stats")
    ResponseEntity<String> getUserStats(
        @RequestParam("userid") String userid
    );

    @PostMapping("/exchange")
    ResponseEntity<Integer> magicExchange(
        @RequestBody String requestBody
    );

    @GetMapping("/user-favorites")
    ResponseEntity<String> getUserFavorites(
        @RequestParam("userid") String userid
    );

    @PostMapping("/remove-favorite")
    ResponseEntity<Integer> removeFavorite(
        @RequestBody String requestBody
    );

    @PostMapping("/add-favorite")
    ResponseEntity<Integer> addFavorite(
        @RequestBody String requestBody
    );

    @PostMapping("/migrate-account")
    ResponseEntity<Integer> migrateAccount(
        @RequestParam("userid") String userid,
        @RequestParam("file") MultipartFile file,
        @RequestParam("uploadtogive") String uploadtogive
    );

    @GetMapping("/begseed-list")
    ResponseEntity<String> getBegSeedList();

    @GetMapping("/begseed-detail")
    ResponseEntity<String> getBegSeedDetail(
        @RequestParam("begid") String begid
    );

    @GetMapping("/begseed-submissions")
    ResponseEntity<String> getBegSeedSubmissions(
        @RequestParam("begid") String begid
    );

    @PostMapping("/submit-seed")
    ResponseEntity<Integer> submitSeed(
        @RequestBody String requestBody
    );

    @PostMapping("/vote-seed")
    ResponseEntity<Integer> voteSeed(
        @RequestBody String requestBody
    );

    @PostMapping("/create-begseed")
    ResponseEntity<Integer> createBegSeed(
        @RequestBody String requestBody
    );

    @GetMapping("/all-seeds")
    ResponseEntity<String> getAllSeeds();

    @GetMapping("/all-seed-promotions")
    ResponseEntity<String> getAllSeedPromotions();

    @PostMapping("/set-seed-promotion")
    ResponseEntity<Integer> createSeedPromotion(
        @RequestBody String requestBody
    );
}