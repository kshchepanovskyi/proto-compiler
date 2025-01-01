package io.protostuff.compiler.parser;

import static io.protostuff.compiler.model.FieldModifier.OPTIONAL;
import static io.protostuff.compiler.model.FieldModifier.REPEATED;
import static io.protostuff.compiler.model.FieldModifier.REQUIRED;

import io.protostuff.compiler.model.AbstractUserTypeContainer;
import io.protostuff.compiler.model.DynamicMessage.Value;
import io.protostuff.compiler.model.Element;
import io.protostuff.compiler.model.Extension;
import io.protostuff.compiler.model.ExtensionContainer;
import io.protostuff.compiler.model.Field;
import io.protostuff.compiler.model.FieldContainer;
import io.protostuff.compiler.model.Group;
import io.protostuff.compiler.model.GroupContainer;
import io.protostuff.compiler.model.Message;
import io.protostuff.compiler.model.MessageContainer;
import io.protostuff.compiler.model.Oneof;
import io.protostuff.compiler.model.Range;
import io.protostuff.compiler.model.SourceCodeLocation;
import io.protostuff.compiler.model.UserType;
import io.protostuff.compiler.model.UserTypeContainer;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Parse listener responsible for processing messages.
 *
 * @author Kostiantyn Shchepanovskyi
 */
public class MessageParseListener extends AbstractProtoParserListener {

    public static final String OPTION_MAP_ENTRY = "map_entry";
    public static final String MAP_ENTRY_KEY = "key";
    public static final String MAP_ENTRY_VALUE = "value";

    public MessageParseListener(BufferedTokenStream tokens, ProtoContext context) {
        super(tokens, context);
    }

    @Override
    public void enterMessageBlock(ProtoParser.MessageBlockContext ctx) {
        UserTypeContainer parent = context.peek(UserTypeContainer.class);
        Message message = new Message(parent);
        context.push(message);
    }

    @Override
    public void exitMessageBlock(ProtoParser.MessageBlockContext ctx) {
        Message message = context.pop(Message.class);
        MessageContainer container = context.peek(MessageContainer.class);
        String name = ctx.messageName().getText();
        message.setName(name);
        message.setSourceCodeLocation(getSourceCodeLocation(ctx));
        container.addMessage(message);
        attachComments(ctx, message, false);
    }

    @Override
    public void exitReservedFieldRanges(ProtoParser.ReservedFieldRangesContext ctx) {
        UserType userType = context.peek(UserType.class);
        List<Range> result = getRanges(userType, ctx.range());
        for (Range range : result) {
            userType.addReservedFieldRange(range);
        }
    }

    @Override
    public void exitReservedFieldNames(ProtoParser.ReservedFieldNamesContext ctx) {
        UserType userType = context.peek(UserType.class);
        for (ProtoParser.ReservedFieldNameContext fieldNameContext : ctx.reservedFieldName()) {
            String fieldName = fieldNameContext.getText();
            fieldName = Util.trimStringName(fieldName);
            userType.addReservedFieldName(fieldName);
        }
    }

    @Override
    public void enterField(ProtoParser.FieldContext ctx) {
        FieldContainer parent = context.peek(FieldContainer.class);
        Field field = new Field(parent);
        context.push(field);
    }

    @Override
    public void exitField(ProtoParser.FieldContext ctx) {
        Field field = context.pop(Field.class);
        final FieldContainer fieldContainer = context.peek(FieldContainer.class);
        updateModifier(ctx.fieldModifier(), field);
        String name = ctx.fieldName().getText();
        field.setName(name);
        Integer tag = Integer.decode(ctx.tag().getText());
        field.setTag(tag);
        field.setIndex(fieldContainer.getFieldCount() + 1);
        String type = ctx.typeReference().getText();
        field.setTypeName(type);
        field.setSourceCodeLocation(getSourceCodeLocation(ctx));
        fieldContainer.addField(field);
        attachComments(ctx, field, true);
    }

    @Override
    public void enterExtendBlock(ProtoParser.ExtendBlockContext ctx) {
        UserTypeContainer parent = context.peek(UserTypeContainer.class);
        Extension extension = new Extension(parent);
        context.push(extension);
    }

    @Override
    public void exitExtendBlock(ProtoParser.ExtendBlockContext ctx) {
        Extension extension = context.pop(Extension.class);
        String extendeeName = ctx.typeReference().getText();
        ExtensionContainer extensionContainer = context.peek(AbstractUserTypeContainer.class);
        extension.setExtendeeName(extendeeName);
        extension.setSourceCodeLocation(getSourceCodeLocation(ctx));
        extensionContainer.addDeclaredExtension(extension);
    }

    @Override
    public void enterGroupBlock(ProtoParser.GroupBlockContext ctx) {
        Element parent = context.peek(Element.class);
        if (parent instanceof Extension) {
            // hack: use extension's parent
            Group group = new Group(((Extension) parent).getParent());
            context.push(group);
        } else if (parent instanceof UserTypeContainer) {
            Group group = new Group((UserTypeContainer) parent);
            context.push(group);
        } else if (parent instanceof Oneof) {
            Oneof oneof = (Oneof) parent;
            Group group = new Group(oneof.getParent());
            context.push(group);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void exitGroupBlock(ProtoParser.GroupBlockContext ctx) {
        Group group = context.pop(Group.class);
        group.setName(ctx.groupName().getText());
        group.setSourceCodeLocation(getSourceCodeLocation(ctx));
        final GroupContainer groupContainer = context.peek(GroupContainer.class);
        final FieldContainer fieldContainer = context.peek(FieldContainer.class);
        Field field = new Field(fieldContainer);
        field.setName(group.getName().toLowerCase()); // same behavior as in protoc
        int tag = Integer.decode(ctx.tag().getText());
        field.setTag(tag);
        field.setIndex(fieldContainer.getFieldCount() + 1);
        field.setTypeName(group.getName());
        field.setType(group);
        field.setSourceCodeLocation(getSourceCodeLocation(ctx));
        groupContainer.addGroup(group);
        fieldContainer.addField(field);
        attachComments(ctx, field, true);
    }

    @Override
    public void enterOneof(ProtoParser.OneofContext ctx) {
        Message parent = context.peek(Message.class);
        Oneof oneof = new Oneof(parent);
        context.push(oneof);
    }

    @Override
    public void exitOneof(ProtoParser.OneofContext ctx) {
        Oneof oneof = context.pop(Oneof.class);
        Message message = context.peek(Message.class);
        oneof.setName(ctx.oneofName().getText());
        oneof.setSourceCodeLocation(getSourceCodeLocation(ctx));
        message.addOneof(oneof);
        attachComments(ctx, oneof, false);
    }

    @Override
    public void enterMap(ProtoParser.MapContext ctx) {
        Message parent = context.peek(Message.class);
        Field field = new Field(parent);
        context.push(field);
    }

    @Override
    public void exitMap(ProtoParser.MapContext ctx) {
        final Field field = context.pop(Field.class);
        final Message message = context.peek(Message.class);
        String name = ctx.fieldName().getText();
        SourceCodeLocation codeLocation = getSourceCodeLocation(ctx);
        Message map = new Message(message);
        String mapEntryTypeName = name + "_entry";
        map.setName(mapEntryTypeName);
        map.setSourceCodeLocation(codeLocation);
        map.getOptions().set(codeLocation, OPTION_MAP_ENTRY, Value.createBoolean(true));
        String keyTypeName = ctx.mapKey().getText();
        Field keyField = createMapKeyField(map, keyTypeName, codeLocation);
        map.addField(keyField);
        String valueTypeName = ctx.mapValue().getText();
        Field valueField = createMapValueField(map, valueTypeName, codeLocation);
        map.addField(valueField);
        Integer tag = Integer.decode(ctx.tag().getText());
        field.setName(name);
        field.setTag(tag);
        field.setIndex(message.getFieldCount() + 1);
        field.setModifier(REPEATED);
        field.setTypeName(mapEntryTypeName);
        field.setType(map);
        field.setSourceCodeLocation(codeLocation);
        message.addField(field);
        message.addMessage(map);
        attachComments(ctx, field, true);
    }

    private Field createMapValueField(Message map, String valueTypeName, SourceCodeLocation codeLocation) {
        Field valueField = new Field(map);
        valueField.setName(MAP_ENTRY_VALUE);
        valueField.setTag(2);
        valueField.setIndex(2);
        valueField.setModifier(OPTIONAL);
        valueField.setTypeName(valueTypeName);
        valueField.setSourceCodeLocation(codeLocation);
        return valueField;
    }

    private Field createMapKeyField(Message map, String keyTypeName, SourceCodeLocation codeLocation) {
        Field keyField = new Field(map);
        keyField.setName(MAP_ENTRY_KEY);
        keyField.setTag(1);
        keyField.setIndex(1);
        keyField.setTypeName(keyTypeName);
        keyField.setModifier(OPTIONAL);
        keyField.setSourceCodeLocation(codeLocation);
        return keyField;
    }

    @Override
    public void exitExtensions(ProtoParser.ExtensionsContext ctx) {
        Message message = context.peek(Message.class);
        List<Range> result = getRanges(message, ctx.range());
        for (Range range : result) {
            message.addExtensionRange(range);
        }
    }

    private List<Range> getRanges(UserType message, List<ProtoParser.RangeContext> ranges) {
        List<Range> result = new ArrayList<>();
        for (ProtoParser.RangeContext rangeContext : ranges) {
            ProtoParser.RangeFromContext fromNode = rangeContext.rangeFrom();
            ProtoParser.RangeToContext toNode = rangeContext.rangeTo();
            TerminalNode maxNode = rangeContext.MAX();
            int from = Integer.decode(fromNode.getText());
            int to;
            if (toNode != null) {
                to = Integer.decode(toNode.getText());
            } else if (maxNode != null) {
                to = Field.MAX_TAG_VALUE;
            } else {
                to = from;
            }
            Range range = new Range(message, from, to);
            range.setSourceCodeLocation(getSourceCodeLocation(rangeContext));
            result.add(range);
        }
        return result;
    }

    private void updateModifier(ProtoParser.FieldModifierContext modifierContext, Field field) {
        if (modifierContext != null) {
            if (modifierContext.OPTIONAL() != null) {
                field.setModifier(OPTIONAL);
            } else if (modifierContext.REQUIRED() != null) {
                field.setModifier(REQUIRED);
            } else if (modifierContext.REPEATED() != null) {
                field.setModifier(REPEATED);
            } else {
                throw new IllegalStateException("not implemented");
            }
        }
    }
}
