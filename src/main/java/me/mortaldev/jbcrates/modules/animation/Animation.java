package me.mortaldev.jbcrates.modules.animation;

import java.util.List;

public enum Animation {
  ORBIT(new OrbitAnimation(), "Orbit", List.of("The contents will orbit around the box."));
  private final IAnimation iAnimation;
  private final String name;
  private final List<String> description;

  Animation(IAnimation iAnimation, String name, List<String> description) {
    this.iAnimation = iAnimation;
    this.name = name;
    this.description = description;
  }

  public IAnimation getIAnimation() {
    return iAnimation;
  }

  public String getName() {
    return name;
  }

  public List<String> getDescription() {
    return description;
  }
}
