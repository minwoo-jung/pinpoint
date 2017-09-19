/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package perftest;

public class Level {

    private String id;
    private int score;
    private Level up;
    private Level down;

    public Level(String id) {
        this.id = id;
    }

    public Level(String id, Level up, Level down) {
        this.id = id;
        this.up = up;
        this.down = down;
    }

    public String enter(String id, int score) {
        String hello = id + "_" + score;
        return hello;
    }

    public void down() {
        if (down != null) {
            down.enter(this.id, ++score);
        }
    }

    public void up() {
        if (up != null) {
            up.enter(this.id, ++score);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Level getUp() {
        return up;
    }

    public void setUp(Level up) {
        this.up = up;
    }

    public Level getDown() {
        return down;
    }

    public void setDown(Level down) {
        this.down = down;
    }

}
