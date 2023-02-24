package future.code.dark.dungeon.service;

import future.code.dark.dungeon.config.Configuration;
import future.code.dark.dungeon.domen.Coin;
import future.code.dark.dungeon.domen.DynamicObject;
import future.code.dark.dungeon.domen.Enemy;
import future.code.dark.dungeon.domen.Exit;
import future.code.dark.dungeon.domen.GameObject;
import future.code.dark.dungeon.domen.Map;
import future.code.dark.dungeon.domen.Player;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static future.code.dark.dungeon.config.Configuration.COIN_CHARACTER;
import static future.code.dark.dungeon.config.Configuration.ENEMIES_ACTIVE;
import static future.code.dark.dungeon.config.Configuration.ENEMY_CHARACTER;
import static future.code.dark.dungeon.config.Configuration.EXIT_CHARACTER;
import static future.code.dark.dungeon.config.Configuration.PLAYER_CHARACTER;

public class GameMaster {

    private static GameMaster instance;

    private final Map map;
    private final List<GameObject> gameObjects;
    public int coins;
    public int collectedCoins;
    private Image victory =new ImageIcon(Configuration.VICTORY_SPRITE).getImage();
    public boolean exitEnabled=false;

    public static synchronized GameMaster getInstance() {
        if (instance == null) {
            instance = new GameMaster();
        }
        return instance;
    }

    private GameMaster() {
        try {
            this.map = new Map(Configuration.MAP_FILE_PATH);
            this.gameObjects = initGameObjects(map.getMap());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private List<GameObject> initGameObjects(char[][] map) {
        List<GameObject> gameObjects = new ArrayList<>();
        Consumer<GameObject> addGameObject = gameObjects::add;
        Consumer<Enemy> addEnemy = enemy -> {if (ENEMIES_ACTIVE) gameObjects.add(enemy);};

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                switch (map[i][j]) {
                    case(EXIT_CHARACTER):
                        addGameObject.accept(new Exit(j, i));
                        break;
                    case(COIN_CHARACTER):
                        addGameObject.accept(new Coin(j, i));
                        coins++;
                        break;
                    case(ENEMY_CHARACTER):
                        addEnemy.accept(new Enemy(j, i));
                        break;
                    case(PLAYER_CHARACTER):
                        addGameObject.accept(new Player(j, i));
                        break;
                }
            }
        }

        return gameObjects;
    }

    public void renderFrame(Graphics graphics) {
        if(!(GameMaster.getInstance().exitEnabled&& getPlayer().getXPosition()==6&&getPlayer().getYPosition()==2)) {
            getMap().render(graphics);
            getStaticObjects().forEach(gameObject -> gameObject.render(graphics));
            getEnemies().forEach(gameObject -> gameObject.render(graphics));
            getPlayer().render(graphics);
            graphics.setColor(Color.WHITE);
            graphics.drawString(getPlayer().toString(), 10, 20);
            graphics.drawString("Всего монет: " + String.valueOf(coins), 10, 40);
            graphics.drawString("Собрано монет: " + String.valueOf(collectedCoins), 10, 60);
        }else{
            graphics.drawImage(victory,175,75,null);
        }
    }

    public Player getPlayer() {
        return (Player) gameObjects.stream()
                .filter(gameObject -> gameObject instanceof Player)
                .findFirst()
                .orElseThrow();
    }
    public Exit getExit() {
        return (Exit) gameObjects.stream()
                .filter(gameObject -> gameObject instanceof Exit)
                .findFirst()
                .orElseThrow();
    }

    private List<GameObject> getStaticObjects() {
        return gameObjects.stream()
                .filter(gameObject -> !(gameObject instanceof DynamicObject))
                .collect(Collectors.toList());
    }

    public List<Enemy> getEnemies() {
        return gameObjects.stream()
                .filter(gameObject -> gameObject instanceof Enemy)
                .map(gameObject -> (Enemy) gameObject)
                .collect(Collectors.toList());
    }

    public List<Coin> getCoins() {
        return gameObjects.stream()
                .filter(gameObject -> gameObject instanceof Coin)
                .map(gameObject -> (Coin) gameObject)
                .collect(Collectors.toList());
    }

    public Map getMap() {
        return map;
    }
    public void deleteEnemies(int x,int y){
        this.gameObjects.removeIf(enemy -> enemy instanceof Enemy&& enemy.getXPosition()==x&&enemy.getYPosition()==y);
    }
    public void deleteCoins(int x,int y){
        coins--;
        collectedCoins++;
        this.gameObjects.removeIf(coin -> coin instanceof Coin&& coin.getXPosition()==x&&coin.getYPosition()==y);
        if(coins==0) exitEnabled=true;
    }
}
