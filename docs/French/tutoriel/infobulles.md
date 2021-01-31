# Ajouter une infobulle

Dans votre classe d'[objet](../../French/tutoriel/objets.md), remplacez la mÃ©thode
`appendTooltip` comme ceci (voir les fichiers [.lang](../../French/tutoriel/lang.md)
[(en)](../../Modding-Tutorials/Miscellaneous/lang.md) pour savoir comment traduire l'infobulle) :

```java
@Override
public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
    tooltip.add(new TranslatableText("item.tutorial.fabric_item.tooltip"));
}
```

Pour un bloc, faites la mÃªme chose mais remplacez la mÃ©thode
`buildTooltip` dans votre classe de [bloc](../../French/tutoriel/blocs.md)
[(en)](../../Modding-Tutorials/Blocks-and-Block-Entities/block.md).
