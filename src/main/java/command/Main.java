package command;

public class Main {

    public static void main(String[] args) {

        Light light = new Light();
        Switch sw1tch = new Switch(light);

        sw1tch.executeCommand();
        System.out.println(light);
        sw1tch.executeCommand();
        System.out.println(light);
        sw1tch.executeCommand();
        System.out.println(light);
    }

    static class Switch {

        private Command lightOffCommand;
        private Command lightOnCommand;
        private Light light;

        public Switch(Light light) {
            this.light = light;
            this.lightOffCommand = new LightOFF(light);
            this.lightOnCommand = new LightON(light);
        }

        public void executeCommand() {
            if (light.isOn())
                lightOffCommand.execute();
            else
                lightOnCommand.execute();
        }
    }

    interface Command {
        void execute();
    }

    static class LightON implements Command {
        private Light light;

        public LightON(Light light) {
            this.light = light;
        }

        @Override
        public void execute() {
            light.turnOn();
        }
    }

    static class LightOFF implements Command {
        private Light light;

        public LightOFF(Light light) {
            this.light = light;
        }

        @Override
        public void execute() {
            light.turnOff();
        }
    }

    static class Light {

        boolean on;

        void turnOn () {
            on = true;
        }

        void turnOff () {
            on = false;
        }

        public boolean isOn(){
            return on;
        }

        @Override
        public String toString() {
            return String.format("Light is %s", on ? "on" : "off");
        }
    }
}
